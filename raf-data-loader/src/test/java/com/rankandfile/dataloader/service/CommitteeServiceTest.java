package com.rankandfile.dataloader.service;

import com.rankandfile.dataloader.entity.Committee;
import com.rankandfile.dataloader.processor.CommitteeProcessor;
import com.rankandfile.dataloader.repository.CommitteeRepository;
import com.rankandfile.dataloader.service.external.committee.CommitteeService;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitteeServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private CommitteeProcessor committeeProcessor;

    @Mock
    private CommitteeRepository committeeRepository;

    private WebClient webClient;

    private CommitteeService committeeService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // Build the WebClient using the baseUrl of the mock server
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addApiKeyQueryParamFilter())
                .build();

        committeeService = new CommitteeService(webClient, committeeProcessor, committeeRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    private ExchangeFilterFunction addApiKeyQueryParamFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            URI updatedUri = UriComponentsBuilder.fromUri(clientRequest.url())
                    .queryParam("api_key", "test_api_key")
                    .build(true)
                    .toUri();

            ClientRequest updatedRequest = ClientRequest.from(clientRequest)
                    .url(updatedUri)
                    .build();

            return Mono.just(updatedRequest);
        });
    }

    @Test
    void testFetchAndProcessCommitteesSuccessfulRetrieval() throws Exception {
        int limit = 2;
        String responsePage1 = "{\"committees\": [{}, {}]}"; // Simulated JSON response with 2 committees
        String responsePage2 = "{\"committees\": [{}]}";

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage1)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage2)
                .addHeader("Content-Type", "application/json"));

        // Mocking CommitteeProcessor behavior
        List<Committee> committeesPage1 = Arrays.asList(new Committee(), new Committee());
        List<Committee> committeesPage2 = Collections.singletonList(new Committee());

        when(committeeProcessor.process(responsePage1)).thenReturn(committeesPage1);
        when(committeeProcessor.process(responsePage2)).thenReturn(committeesPage2);

        // Execute the method
        List<Committee> result = committeeService.fetchAndProcessCommittees(limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(3, result.size()); // 2 from first page, 1 from second page

        verify(committeeRepository).saveAll(result);
        verify(committeeProcessor).process(responsePage1);
        verify(committeeProcessor).process(responsePage2);

        // Verify that the requests were made as expected
        RecordedRequest request1 = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        RecordedRequest request2 = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request1, "First request was not made to the MockWebServer");
        assertNotNull(request2, "Second request was not made to the MockWebServer");
        assertEquals("GET", request1.getMethod());
        assertEquals("GET", request2.getMethod());
        assertTrue(request1.getPath().contains("api_key=test_api_key"));
        assertTrue(request2.getPath().contains("api_key=test_api_key"));

        // Verify that the offsets are correct
        assertTrue(request1.getPath().contains("offset=0"));
        assertTrue(request2.getPath().contains("offset=2"));
    }

    @Test
    void testFetchAndProcessCommitteesEmptyResponse() throws Exception {
        int limit = 2;
        String response = "";

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<Committee> result = committeeService.fetchAndProcessCommittees(limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that committeeRepository.saveAll is called with an empty list
        verify(committeeRepository).saveAll(result);

        // Verify that the processor is not called due to empty response
        verify(committeeProcessor, never()).process(anyString());

        // Verify that the request was made
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessCommitteesExceptionDuringFetch() throws Exception {
        int limit = 2;

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            committeeService.fetchAndProcessCommittees(limit);
        });

        assertNotNull(exception);
        verify(committeeRepository, never()).saveAll(any());
        verify(committeeProcessor, never()).process(any());

        // Verify that the request was made
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessCommitteesExceptionDuringProcessing() throws Exception {
        int limit = 2;
        String response = "{\"committees\": [{}, {}]}";

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking CommitteeProcessor to throw an exception
        when(committeeProcessor.process(response)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            committeeService.fetchAndProcessCommittees(limit);
        });

        assertNotNull(exception);
        assertEquals("Processing error", exception.getMessage());
        verify(committeeRepository, never()).saveAll(any());
        verify(committeeProcessor).process(response);

        // Verify that the request was made
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessCommitteesNoCommitteesReturned() throws Exception {
        int limit = 2;
        String response = "{\"committees\": []}"; // Empty committees array

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking CommitteeProcessor behavior
        when(committeeProcessor.process(response)).thenReturn(Collections.emptyList());

        // Execute the method
        List<Committee> result = committeeService.fetchAndProcessCommittees(limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(0, result.size());

        // Verify that committeeRepository.saveAll is called with an empty list
        verify(committeeRepository).saveAll(result);

        // Verify that the processor was called with the response
        verify(committeeProcessor).process(response);

        // Verify that the request was made
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request, "Request was not made to the MockWebServer");
    }
}
