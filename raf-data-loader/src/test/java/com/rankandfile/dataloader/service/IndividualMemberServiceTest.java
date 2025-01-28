package com.rankandfile.dataloader.service;

import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.processor.PersonProcessor;
import com.rankandfile.dataloader.repository.PersonRepository;
import com.rankandfile.dataloader.service.external.person.IndividualMemberService;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndividualMemberServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private PersonProcessor personProcessor;

    @Mock
    private PersonRepository personRepository;

    private WebClient webClient;

    private IndividualMemberService individualMemberService;

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

        individualMemberService = new IndividualMemberService(personProcessor, webClient, personRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    private ExchangeFilterFunction addApiKeyQueryParamFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            URI updatedUri = UriComponentsBuilder.fromUri(clientRequest.url())
                    .queryParam("api_key", "test_api_key") // Use a test API key
                    .build(true)
                    .toUri();

            ClientRequest updatedRequest = ClientRequest.from(clientRequest)
                    .url(updatedUri)
                    .build();

            return Mono.just(updatedRequest);
        });
    }

    @Test
    void testFetchAndProcessPersonSuccessfulRetrieval() throws Exception {
        String bioguideId = "A000360";
        String response = "{\"personData\": \"valid data\"}";
        Person expectedPerson = new Person();
        expectedPerson.setPersonId("A000360");

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking the processor to return a Person object
        when(personProcessor.validatePerson(response)).thenReturn(expectedPerson);

        // Execute the method
        Person result = individualMemberService.fetchAndProcessPerson(bioguideId);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(expectedPerson, result);
        verify(personRepository).save(expectedPerson);

        // Verify that the request was made as expected
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("/member/" + bioguideId));
        assertTrue(recordedRequest.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testFetchAndProcessPersonEmptyResponse() throws Exception {
        String bioguideId = "A000360";
        String response = "";

        // Enqueue the mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        Person result = individualMemberService.fetchAndProcessPerson(bioguideId);

        // Verify that the result is null
        assertNull(result);

        // Verify that the repository's save method was not called
        verify(personRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessPersonNullResponse() throws Exception {
        String bioguideId = "A000360";

        // Enqueue a response without setting the body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        Person result = individualMemberService.fetchAndProcessPerson(bioguideId);

        // Verify that the result is null
        assertNull(result);

        // Verify that the repository's save method was not called
        verify(personRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessPersonExceptionDuringFetch() throws Exception {
        String bioguideId = "A000360";

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            individualMemberService.fetchAndProcessPerson(bioguideId);
        });

        assertNotNull(exception);
        verify(personRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessPersonExceptionDuringProcessing() throws Exception {
        String bioguideId = "A000360";
        String response = "{\"personData\": \"valid data\"}";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(personProcessor.validatePerson(response)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            individualMemberService.fetchAndProcessPerson(bioguideId);
        });

        assertNotNull(exception);
        verify(personRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchAndProcessPersonInvalidBioguideId() throws InterruptedException {
        String bioguideId = "";

        // Execute the method and expect an exception due to invalid bioguideId
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            individualMemberService.fetchAndProcessPerson(bioguideId);
        });

        assertNotNull(exception);
        verify(personRepository, never()).save(any());

        // Since the bioguideId is invalid, the web client should not make a request
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, java.util.concurrent.TimeUnit.SECONDS);
        assertNull(recordedRequest, "No request should have been made to the MockWebServer");
    }
}
