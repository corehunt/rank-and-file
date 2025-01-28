package com.rankandfile.dataloader.service;

import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.processor.CongressMemberProcessor;
import com.rankandfile.dataloader.repository.PersonRepository;
import com.rankandfile.dataloader.service.external.person.MembersService;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MembersServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private CongressMemberProcessor congressMemberProcessor;

    @Mock
    private PersonRepository personRepository;

    private WebClient webClient;

    private MembersService membersService;

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

        membersService = new MembersService(webClient, congressMemberProcessor, personRepository);
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
    void testFetchAndSaveMembersSuccessfulRetrieval() throws Exception {
        int limit = 2;
        String responsePage1 = "{\"members\": [{}, {}]}"; // Simulated JSON response
        String responsePage2 = "{\"members\": [{}]}";     // Less than limit to stop pagination

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage1)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage2)
                .addHeader("Content-Type", "application/json"));

        // Mocking CongressMemberProcessor behavior
        List<Person> membersPage1 = Arrays.asList(new Person(), new Person());
        List<Person> membersPage2 = Collections.singletonList(new Person());

        when(congressMemberProcessor.processMembers(responsePage1)).thenReturn(membersPage1);
        when(congressMemberProcessor.processMembers(responsePage2)).thenReturn(membersPage2);

        // Execute the method
        List<Person> result = membersService.fetchAndSaveMembers(limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(3, result.size()); // 2 from first page, 1 from second page
        verify(personRepository).saveAll(result);

        // Verify that the requests were made as expected
        RecordedRequest request1 = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        RecordedRequest request2 = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request1, "First request was not made to the MockWebServer");
        assertNotNull(request2, "Second request was not made to the MockWebServer");
        assertEquals("GET", request1.getMethod());
        assertEquals("GET", request2.getMethod());
        assertTrue(request1.getPath().contains("/member"));
        assertTrue(request2.getPath().contains("/member"));
        assertTrue(request1.getPath().contains("api_key=test_api_key"));
        assertTrue(request2.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testFetchAndSaveMembersEmptyResponse() throws Exception {
        int limit = 2;
        String response = "";

        // Enqueue the mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<Person> result = membersService.fetchAndSaveMembers(limit);

        // Verify that the result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that processor was not called since response is empty
        verify(congressMemberProcessor, never()).processMembers(anyString());

        // Verify that repository saveAll was not called
        verify(personRepository, never()).saveAll(anyList());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchAndSaveMembersNullResponse() throws Exception {
        int limit = 2;

        // Enqueue a response without setting the body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<Person> result = membersService.fetchAndSaveMembers(limit);

        // Verify that the result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that processor was not called since response is null
        verify(congressMemberProcessor, never()).processMembers(anyString());

        // Verify that repository saveAll was not called
        verify(personRepository, never()).saveAll(anyList());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchAndSaveMembersExceptionDuringFetch() {
        int limit = 2;

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            membersService.fetchAndSaveMembers(limit);
        });

        assertNotNull(exception);
        verify(congressMemberProcessor, never()).processMembers(anyString());
        verify(personRepository, never()).saveAll(anyList());
    }

    @Test
    void testFetchAndSaveMembersExceptionDuringProcessing() {
        int limit = 2;
        String response = "{\"members\": [{}, {}]}";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(congressMemberProcessor.processMembers(response)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            membersService.fetchAndSaveMembers(limit);
        });

        assertNotNull(exception);
        assertEquals("Processing error", exception.getMessage());
        verify(personRepository, never()).saveAll(anyList());
    }
}
