package com.rankandfile.dataloader.service;

import com.rankandfile.dataloader.entity.Action;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.processor.BillActionProcessor;
import com.rankandfile.dataloader.repository.ActionRepository;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.service.external.bill.BillActionService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillActionServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private BillActionProcessor billActionProcessor;

    private WebClient webClient;

    private BillActionService billActionService;

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

        billActionService = new BillActionService(webClient, billRepository, actionRepository, billActionProcessor);
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
    void testGetActionsByBillNumberSuccessfulRetrieval() throws Exception {
        String congressNo = "117";
        String billType = "HR";
        String billNo = "1234";
        int limit = 2;
        String responsePage1 = "{\"actions\": [{}, {}]}"; // Simulated JSON response
        String responsePage2 = "{\"actions\": [{}]}";     // Less than limit to stop pagination

        Bill bill = new Bill();
        bill.setBillId("117-1234");
        bill.setBillNo(billNo);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue mock responses
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage1)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePage2)
                .addHeader("Content-Type", "application/json"));

        // Mocking BillActionProcessor behavior
        List<Action> actionsPage1 = Arrays.asList(new Action(), new Action());
        List<Action> actionsPage2 = Collections.singletonList(new Action());

        when(billActionProcessor.processActionList(responsePage1, bill)).thenReturn(actionsPage1);
        when(billActionProcessor.processActionList(responsePage2, bill)).thenReturn(actionsPage2);

        // Execute the method
        List<Action> result = billActionService.getActionsByBillNumber(congressNo, billType, billNo, limit);

        // Verify interactions and results
        assertNotNull(result);
        assertEquals(3, result.size()); // 2 from first page, 1 from second page
        verify(actionRepository).saveAll(result);
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);

        // Verify that the requests were made as expected
        RecordedRequest request1 = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        RecordedRequest request2 = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(request1, "First request was not made to the MockWebServer");
        assertNotNull(request2, "Second request was not made to the MockWebServer");
        assertEquals("GET", request1.getMethod());
        assertEquals("GET", request2.getMethod());
        assertTrue(request1.getPath().contains("api_key=test_api_key"));
        assertTrue(request2.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testGetActionsByBillNumberEmptyResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";
        int limit = 2;
        String response = "";

        Bill bill = new Bill();
        bill.setBillId("HR1171234");
        bill.setBillNo(billNo);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue the mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<Action> result = billActionService.getActionsByBillNumber(congressNo, billType, billNo, limit);

        // Verify that the result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetActionsByBillNumberNullResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";
        int limit = 2;

        Bill bill = new Bill();
        bill.setBillId("HR1171234");
        bill.setBillNo(billNo);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue a response without setting the body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        List<Action> result = billActionService.getActionsByBillNumber(congressNo, billType, billNo, limit);

        // Verify that the result is empty
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetActionsByBillNumberExceptionDuringFetch() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";
        int limit = 2;

        Bill bill = new Bill();
        bill.setBillId("HR1171234");
        bill.setBillNo(billNo);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            billActionService.getActionsByBillNumber(congressNo, billType, billNo, limit);
        });

        assertNotNull(exception);
        verify(actionRepository, never()).saveAll(any());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetActionsByBillNumberExceptionDuringProcessing() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "1234";
        int limit = 2;
        String response = "{\"actions\": [{}, {}]}";

        Bill bill = new Bill();
        bill.setBillId("HR1171234");
        bill.setBillNo(billNo);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(billActionProcessor.processActionList(response, bill)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billActionService.getActionsByBillNumber(congressNo, billType, billNo, limit);
        });

        assertNotNull(exception);
        verify(actionRepository, never()).saveAll(any());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }
}