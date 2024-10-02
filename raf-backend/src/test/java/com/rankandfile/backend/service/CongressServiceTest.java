package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Congress;
import com.rankandfile.backend.processor.CongressProcessor;
import com.rankandfile.backend.repository.CongressRepository;
import com.rankandfile.backend.service.external.congress.CongressService;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CongressServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private CongressProcessor congressProcessor;

    @Mock
    private CongressRepository congressRepository;

    private WebClient webClient;

    private CongressService congressService;

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

        congressService = new CongressService(webClient, congressRepository, congressProcessor);
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
    void testFetchAndSaveCongressByNumberSuccessfulRetrieval() throws Exception {
        String congressNo = "117";
        String response = "{\"congress\": {\"number\": 117}}";

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking CongressProcessor behavior
        Congress congress = new Congress();
        when(congressProcessor.processCongressData(response)).thenReturn(congress);

        // Execute the method
        List<Congress> result = congressService.fetchAndSaveCongressByNumber(congressNo);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(congress, result.get(0));
        verify(congressRepository).save(congress);

        // Verify that the request was made as expected
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("congress/" + congressNo));
        assertTrue(recordedRequest.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testFetchAndSaveCongressByNumberExceptionDuringFetch() {
        String congressNo = "117";

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            congressService.fetchAndSaveCongressByNumber(congressNo);
        });

        assertNotNull(exception);
        verify(congressRepository, never()).save(any());
    }

    @Test
    void testFetchAndSaveCongressByNumberExceptionDuringProcessing() {
        String congressNo = "117";
        String response = "{\"congress\": {\"number\": 117}}";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(congressProcessor.processCongressData(response)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            congressService.fetchAndSaveCongressByNumber(congressNo);
        });

        assertNotNull(exception);
        verify(congressRepository, never()).save(any());
    }

}
