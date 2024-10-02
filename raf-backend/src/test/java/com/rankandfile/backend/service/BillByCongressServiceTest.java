package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillByCongressProcessor;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.service.external.bill.BillByCongressService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class BillByCongressServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillByCongressProcessor billByCongressProcessor;

    private WebClient webClient;

    private BillByCongressService billByCongressService;

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

        billByCongressService = new BillByCongressService(webClient, billRepository, billByCongressProcessor);
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
    void testWebClientCommunication() throws Exception {
        String response = "Hello World";
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "text/plain"));

        String result = webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertEquals(response, result);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }


    @Test
    void testGetBillsByCongressEmptyResponse() throws Exception {
        Integer congressNo = 117;
        int limit = 2;
        String response = "";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method with the test limit
        List<Bill> result = billByCongressService.getBillsByCongress(congressNo, limit);

        // Verify interactions and results
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testGetBillsByCongressNullResponse() throws Exception {
        Integer congressNo = 117;
        int limit = 2;

        // Enqueue a response without a body to simulate a null response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // Execute the method with the test limit
        List<Bill> result = billByCongressService.getBillsByCongress(congressNo, limit);

        // Verify interactions and results
        assertNotNull(result); // The service should handle null response gracefully
        assertTrue(result.isEmpty());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
        assertEquals("GET", recordedRequest.getMethod());
    }


    @Test
    void testGetBillsByCongressEmptyBillList() throws Exception {
        Integer congressNo = 117;
        int limit = 2;
        String response = "some valid response";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking BillByCongressProcessor behavior to return an empty list
        when(billByCongressProcessor.processBillList(response)).thenReturn(Collections.emptyList());

        // Execute the method with the test limit
        List<Bill> result = billByCongressService.getBillsByCongress(congressNo, limit);

        // Verify interactions and results
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testGetBillsByCongressExceptionDuringFetch() throws Exception {
        Integer congressNo = 117;
        int limit = 2;

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billByCongressService.getBillsByCongress(congressNo, limit);
        });

        assertNotNull(exception);
        verify(billRepository, never()).saveAll(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testGetBillsByCongressExceptionDuringProcessing() throws Exception {
        Integer congressNo = 117;
        int limit = 2;
        String response = "some valid response";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking BillByCongressProcessor to throw an exception
        when(billByCongressProcessor.processBillList(response))
                .thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billByCongressService.getBillsByCongress(congressNo, limit);
        });

        assertNotNull(exception);
        verify(billRepository, never()).saveAll(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testGetBillsByCongressMultipleIterations() throws Exception {
        Integer congressNo = 117;
        int limit = 2;
        String responsePage1 = "response page 1";
        String responsePage2 = "response page 2";

        // Create bill lists
        List<Bill> billListPage1 = Arrays.asList(new Bill(), new Bill());
        List<Bill> billListPage2 = Arrays.asList(new Bill());

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage1)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage2)
                .addHeader("Content-Type", "application/json"));

        // Mocking BillByCongressProcessor behavior
        when(billByCongressProcessor.processBillList(responsePage1)).thenReturn(billListPage1);
        when(billByCongressProcessor.processBillList(responsePage2)).thenReturn(billListPage2);

        // Execute the method
        List<Bill> result = billByCongressService.getBillsByCongress(congressNo, limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(3, result.size()); // 2 from first page, 1 from second page
        verify(billRepository).saveAll(result);

        // Verify that the requests were made as expected
        RecordedRequest request1 = mockWebServer.takeRequest();
        RecordedRequest request2 = mockWebServer.takeRequest();
        assertEquals("GET", request1.getMethod());
        assertEquals("GET", request2.getMethod());
    }

    @Test
    void testGetBillsByCongressExactMultipleOfLimit() throws Exception {
        Integer congressNo = 117;
        int limit = 2;
        String response = "some valid response";

        // Create bill list with size equal to limit
        List<Bill> billList = Arrays.asList(new Bill(), new Bill());

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        // Mocking BillByCongressProcessor behavior
        when(billByCongressProcessor.processBillList(response))
                .thenReturn(billList);

        // Execute the method
        List<Bill> result = billByCongressService.getBillsByCongress(congressNo, limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(4, result.size()); // 2 bills from first call + 2 bills from second call
        verify(billRepository).saveAll(result);

        // Verify that the requests were made as expected
        RecordedRequest request1 = mockWebServer.takeRequest();
        RecordedRequest request2 = mockWebServer.takeRequest();
        RecordedRequest request3 = mockWebServer.takeRequest();
        assertEquals("GET", request1.getMethod());
        assertEquals("GET", request2.getMethod());
        assertEquals("GET", request3.getMethod());
    }
}