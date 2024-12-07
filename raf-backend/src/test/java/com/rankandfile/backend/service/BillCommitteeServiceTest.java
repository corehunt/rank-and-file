package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillCommitteeProcessor;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.service.external.bill.BillCommitteeService;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillCommitteeServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private WebClient webClient;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillCommitteeProcessor billCommitteeProcessor;

    private BillCommitteeService billCommitteeService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // Build the WebClient using the baseUrl of the mock server
        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addApiKeyQueryParamFilter());

        this.webClient = webClientBuilder.build();

        billCommitteeService = new BillCommitteeService(
                webClient, billRepository, billCommitteeProcessor);
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
    void testGetCommitteesByBillNumberBillNotFound() {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;

        // Mock the BillRepository to return null
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(null);

        // Execute the method
        billCommitteeService.getCommitteesByBillNumber(congressNo, billType, billNo);

        // Verify interactions
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verifyNoMoreInteractions(billRepository);
        verifyNoInteractions(billCommitteeProcessor);

        // No request should be made to the MockWebServer
        assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    void testGetCommitteesByBillNumberEmptyResponse() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;
        String responseBody = ""; // Empty response

        // Mock the BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId("BILL-1234");
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // Execute the method
        billCommitteeService.getCommitteesByBillNumber(congressNo, billType, billNo);

        // Verify interactions
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verifyNoInteractions(billCommitteeProcessor);
        verify(billRepository, never()).save(any());

        // Verify the request made to the MockWebServer
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetCommitteesByBillNumberExceptionDuringFetch() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;

        // Mock the BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId("BILL-1234");
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "application/json"));

        // Execute the method and expect an exception
        Exception exception = assertThrows(Exception.class, () -> {
            billCommitteeService.getCommitteesByBillNumber(congressNo, billType, billNo);
        });

        assertNotNull(exception);
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verifyNoInteractions(billCommitteeProcessor);
        verify(billRepository, never()).save(any());

        // Verify the request made to the MockWebServer
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }

    @Test
    void testGetCommitteesByBillNumberExceptionDuringProcessing() throws Exception {
        Integer congressNo = 117;
        String billType = "hr";
        Integer billNo = 1234;
        String responseBody = "{\"committees\": [{}, {}]}"; // Simulated JSON response

        // Mock the BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId("BILL-1234");
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType)).thenReturn(bill);

        // Enqueue the mock response
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // Mock the BillCommitteeProcessor to throw an exception
        doThrow(new RuntimeException("Processing error"))
                .when(billCommitteeProcessor).process(responseBody, bill.getBillId());

        // Execute the method and expect an exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billCommitteeService.getCommitteesByBillNumber(congressNo, billType, billNo);
        });

        assertNotNull(exception);
        assertEquals("Processing error", exception.getMessage());
        verify(billRepository).findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        verify(billCommitteeProcessor).process(responseBody, bill.getBillId());
        verify(billRepository, never()).save(any());

        // Verify the request made to the MockWebServer
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "No request was made to the MockWebServer");
    }
}
