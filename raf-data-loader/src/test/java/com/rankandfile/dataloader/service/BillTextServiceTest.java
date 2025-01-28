package com.rankandfile.dataloader.service;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Text;
import com.rankandfile.dataloader.processor.BillTextProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.repository.TextRepository;
import com.rankandfile.dataloader.service.external.bill.BillTextService;
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

class BillTextServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private TextRepository textRepository;

    @Mock
    private BillTextProcessor billTextProcessor;

    private WebClient webClient;

    private BillTextService billTextService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addApiKeyQueryParamFilter())
                .build();

        billTextService = new BillTextService(webClient, billRepository, billTextProcessor, textRepository);
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
    void testFetchBillTextsSuccessfulRetrieval() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String response = "{ \"textVersions\": [ { \"date\": \"2022-02-15T05:00:00Z\", \"type\": \"Placed on Calendar Senate\", \"formats\": [] } ] }";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mocking BillTextProcessor behavior
        List<Text> textList = Collections.singletonList(new Text());
        when(billTextProcessor.processBillTextResponse(response, bill)).thenReturn(textList);

        // Execute the method
        billTextService.fetchBillTexts(congressNo, billType, billNumber);

        // Verify interactions
        verify(billTextProcessor, times(1)).processBillTextResponse(response, bill);
        verify(textRepository, times(1)).saveAll(textList);
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testFetchBillTextsBillNotFound() {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999";

        // Mock billRepository to return null
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(null);

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            billTextService.fetchBillTexts(congressNo, billType, billNumber);
        });

        assertNotNull(exception);
        assertTrue(exception instanceof jakarta.persistence.EntityNotFoundException);
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);

        // No requests should have been made to the MockWebServer
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    void testFetchBillTextsEmptyResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String response = "";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        billTextService.fetchBillTexts(congressNo, billType, billNumber);

        // Verify that processor and repository methods are not called
        verify(billTextProcessor, never()).processBillTextResponse(anyString(), any(Bill.class));
        verify(textRepository, never()).saveAll(anyList());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchBillTextsNullResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue mock response without setting body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        billTextService.fetchBillTexts(congressNo, billType, billNumber);

        // Verify that processor and repository methods are not called
        verify(billTextProcessor, never()).processBillTextResponse(anyString(), any(Bill.class));
        verify(textRepository, never()).saveAll(anyList());

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchBillTextsExceptionDuringFetch() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            billTextService.fetchBillTexts(congressNo, billType, billNumber);
        });

        assertNotNull(exception);
        verify(billTextProcessor, never()).processBillTextResponse(anyString(), any(Bill.class));
        verify(textRepository, never()).saveAll(anyList());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
    }

    @Test
    void testFetchBillTextsExceptionDuringProcessing() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String response = "{ \"textVersions\": [ { \"date\": \"2022-02-15T05:00:00Z\", \"type\": \"Placed on Calendar Senate\", \"formats\": [] } ] }";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return a Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        when(billTextProcessor.processBillTextResponse(response, bill)).thenThrow(new RuntimeException("Processing error"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billTextService.fetchBillTexts(congressNo, billType, billNumber);
        });

        assertNotNull(exception);
        assertEquals("Processing error", exception.getMessage());
        verify(textRepository, never()).saveAll(anyList());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "Request was not made to the MockWebServer");
    }
}
