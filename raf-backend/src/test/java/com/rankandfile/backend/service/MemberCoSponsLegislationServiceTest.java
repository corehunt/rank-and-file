package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.processor.SponsoredLegislationProcessor;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.service.external.person.MemberCoSponsLegislationService;
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

class MemberCoSponsLegislationServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private SponsoredLegislationProcessor sponsoredLegislationProcessor;

    @Mock
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    private WebClient webClient;

    private MemberCoSponsLegislationService memberCoSponsLegislationService;

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

        memberCoSponsLegislationService = new MemberCoSponsLegislationService(webClient, sponsoredLegislationProcessor, sponsoredLegislationRepository);
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
    void testGetCoSponsoredLegislationByPersonIdSuccessfulRetrieval() throws Exception {
        String personId = "A000360";
        int limit = 2;
        String responsePage1 = "{\"cosponsoredLegislation\": [{}, {}]}";
        String responsePage2 = "{\"cosponsoredLegislation\": [{}]}";

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage1)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage2)
                .addHeader("Content-Type", "application/json"));

        // Mocking SponsoredLegislationProcessor behavior
        List<SponsoredLegislation> legislationPage1 = Arrays.asList(new SponsoredLegislation(), new SponsoredLegislation());
        List<SponsoredLegislation> legislationPage2 = Collections.singletonList(new SponsoredLegislation());

        when(sponsoredLegislationProcessor.process(responsePage1, personId)).thenReturn(legislationPage1);
        when(sponsoredLegislationProcessor.process(responsePage2, personId)).thenReturn(legislationPage2);

        // Execute the method
        List<SponsoredLegislation> result = memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(personId, limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(3, result.size()); // 2 from first page, 1 from second page
        verify(sponsoredLegislationRepository).saveAll(result);

        // Verify that the requests were made as expected
        RecordedRequest request1 = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        RecordedRequest request2 = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(request1, "First request was not made to the MockWebServer");
        assertNotNull(request2, "Second request was not made to the MockWebServer");
        assertEquals("GET", request1.getMethod());
        assertEquals("GET", request2.getMethod());
        assertTrue(request1.getPath().contains("member/" + personId + "/cosponsored-legislation"));
        assertTrue(request2.getPath().contains("member/" + personId + "/cosponsored-legislation"));
        assertTrue(request1.getPath().contains("api_key=test_api_key"));
        assertTrue(request2.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testGetCoSponsoredLegislationByPersonIdEmptyResponse() {
        String personId = "A000360";
        int limit = 2;
        String response = "";

        // Enqueue the mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<SponsoredLegislation> result = memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(personId, limit);

        // Verify that the result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCoSponsoredLegislationByPersonIdNullResponse() {
        String personId = "A000360";
        int limit = 2;

        // Enqueue a response without setting the body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<SponsoredLegislation> result = memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(personId, limit);

        // Verify that the result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test
    void testGetCoSponsoredLegislationByPersonIdExceptionDuringFetch() {
        String personId = "A000360";
        int limit = 2;

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<SponsoredLegislation> result = memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(personId, limit);

        // Verify that the result is empty (since the exception is caught in the service)
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that saveAll was not called
        verify(sponsoredLegislationRepository, never()).saveAll(any());
    }

    @Test
    void testGetCoSponsoredLegislationByPersonIdExceptionDuringProcessing(){
        String personId = "A000360";
        int limit = 2;
        String response = "{\"cosponsoredLegislation\": [{}, {}]}";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(sponsoredLegislationProcessor.process(response, personId)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method
        List<SponsoredLegislation> result = memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(personId, limit);

        // Verify that the result is empty (since the exception is caught in the service)
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that saveAll was not called
        verify(sponsoredLegislationRepository, never()).saveAll(any());
    }

}
