package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillSummaryProcessor;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.service.external.bill.BillSummaryService;
import jakarta.persistence.EntityNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillSummaryServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillSummaryProcessor billSummaryProcessor;

    private WebClient webClient;

    private BillSummaryService billSummaryService;

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

        billSummaryService = new BillSummaryService(webClient, billRepository, billSummaryProcessor);
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
    void testFetchBillSummarySuccessfulRetrieval() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNumber = 3076;
        String jsonResponse = "{\n" +
                "  \"summaries\": [\n" +
                "    {\n" +
                "      \"actionDate\": \"2022-04-06\",\n" +
                "      \"actionDesc\": \"Public Law\",\n" +
                "      \"text\": \"<p>Summary Text</p>\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setCongress(congressNo);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Mocking BillSummaryProcessor behavior
        doReturn(bill).when(billSummaryProcessor).processBillSummaryResponse(jsonResponse, bill);

        // Execute the method
        billSummaryService.fetchBillSummary(congressNo, billType, billNumber);

        // Verify interactions
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        verify(billSummaryProcessor).processBillSummaryResponse(jsonResponse, bill);
        verify(billRepository).save(bill);

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
        assertEquals("GET", recordedRequest.getMethod());
        assertTrue(recordedRequest.getPath().contains("api_key=test_api_key"));
    }

    @Test
    void testFetchBillSummaryEmptyResponse() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNumber = 3076;
        String jsonResponse = "";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setCongress(congressNo);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue mock response with empty body
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        billSummaryService.fetchBillSummary(congressNo, billType, billNumber);

        // Verify that the processor is not called
        verify(billSummaryProcessor, never()).processBillSummaryResponse(anyString(), any(Bill.class));
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchBillSummaryNullResponse() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNumber = 3076;

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setCongress(congressNo);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue a response without setting the body (simulating null response)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        billSummaryService.fetchBillSummary(congressNo, billType, billNumber);

        // Verify that the processor is not called
        verify(billSummaryProcessor, never()).processBillSummaryResponse(anyString(), any(Bill.class));
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchBillSummaryExceptionDuringFetch() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNumber = 3076;

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setCongress(congressNo);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            billSummaryService.fetchBillSummary(congressNo, billType, billNumber);
        });

        assertNotNull(exception);
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        verify(billSummaryProcessor, never()).processBillSummaryResponse(anyString(), any(Bill.class));
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchBillSummaryExceptionDuringProcessing() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNumber = 3076;
        String jsonResponse = "{\n" +
                "  \"summaries\": [\n" +
                "    {\n" +
                "      \"actionDate\": \"2022-04-06\",\n" +
                "      \"actionDesc\": \"Public Law\",\n" +
                "      \"text\": \"<p>Summary Text</p>\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setCongress(congressNo);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(bill);

        // Enqueue mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Mock the processor to throw an exception
        doThrow(new RuntimeException("Processing error")).when(billSummaryProcessor).processBillSummaryResponse(jsonResponse, bill);

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billSummaryService.fetchBillSummary(congressNo, billType, billNumber);
        });

        assertNotNull(exception);
        assertEquals("Processing error", exception.getMessage());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        verify(billSummaryProcessor).processBillSummaryResponse(jsonResponse, bill);
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the request was made
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testFetchBillSummaryBillNotFound() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNumber = 9999;

        // Mock billRepository to return null
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType)).thenReturn(null);

        // Execute the method and expect an exception
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            billSummaryService.fetchBillSummary(congressNo, billType, billNumber);
        });

        assertNotNull(exception);
        assertEquals("Bill not found", exception.getMessage());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        verifyNoInteractions(billSummaryProcessor);
        verify(billRepository, never()).save(any(Bill.class));
    }
}
