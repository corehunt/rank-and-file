package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillByCongressTypeNumberProcessor;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.service.external.bill.BillByTypeAndNumberService;
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

class BillByTypeAndNumberServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillByCongressTypeNumberProcessor billProcessor;

    private WebClient webClient;

    private BillByTypeAndNumberService billService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // Build the WebClient using the baseUrl of the mock server
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addApiKeyQueryParamFilter())
                .build();

        billService = new BillByTypeAndNumberService(webClient, billRepository, billProcessor);
    }

    @AfterEach
    public void tearDown() throws Exception {
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
    public void testGetBillByTypeAndNumberSuccessfulRetrieval() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;
        String response = "{\"billData\": \"valid data\"}";
        Bill expectedBill = new Bill();
        expectedBill.setBillNo(billNo);

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking the processor to return a Bill object
        when(billProcessor.process(response)).thenReturn(expectedBill);

        // Execute the method
        Bill result = billService.getBillByTypeAndNumber(congressNo, billType, billNo);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(expectedBill, result);
        verify(billRepository).save(expectedBill);

        // Verify that the request was made as expected
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("api_key=test_api_key"));
        assertTrue(recordedRequest.getPath().contains("/bill/117/hr/1234"));
    }

    @Test
    public void testGetBillByTypeAndNumberEmptyResponse() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;
        String response = "";

        // Enqueue the mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        Bill result = billService.getBillByTypeAndNumber(congressNo, billType, billNo);

        // Verify that the result is null
        assertNull(result);

        // Verify that the repository's save method was not called
        verify(billRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    public void testGetBillByTypeAndNumberNullResponse() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;

        // Enqueue a response without setting the body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        Bill result = billService.getBillByTypeAndNumber(congressNo, billType, billNo);

        // Verify that the result is null
        assertNull(result);

        // Verify that the repository's save method was not called
        verify(billRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    public void testGetBillByTypeAndNumberExceptionDuringFetch() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            billService.getBillByTypeAndNumber(congressNo, billType, billNo);
        });

        assertNotNull(exception);
        verify(billRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    public void testGetBillByTypeAndNumberExceptionDuringProcessing() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;
        String response = "{\"billData\": \"valid data\"}";

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(billProcessor.process(response)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billService.getBillByTypeAndNumber(congressNo, billType, billNo);
        });

        assertNotNull(exception);
        verify(billRepository, never()).save(any());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

}