package com.rankandfile.dataloader.service.scheduled;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.processor.RecentBillProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScheduledNewBillCreationServiceTest {

    private MockWebServer mockWebServer;
    private WebClient webClient;
    private AutoCloseable closeable;

    @Mock
    private RecentBillProcessor recentBillProcessor;

    @Mock
    private BillRepository billRepository;

    @Mock
    private ScheduledBillHydrationRunner hydrationRunner;

    private ScheduledNewBillCreationService scheduledService;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        scheduledService = new ScheduledNewBillCreationService(
                webClient,
                recentBillProcessor,
                billRepository,
                hydrationRunner);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
        closeable.close();
    }

    /**
     * Test that loadRecentBills() processes a valid response, saves the bills,
     * and calls the hydration service.
     */
    @Test
    void testLoadRecentBillssuccessfulFlow() throws Exception {
        // Prepare sample JSON response with a non-empty bills array.
        String jsonPage1 = "{ \"bills\": [ " +
                "{ \"congress\": \"119\", \"latestAction\": { \"actionDate\": \"2025-01-27\", \"text\": \"Action text 1.\" }, " +
                "\"number\": \"40\", \"originChamber\": \"Senate\", \"originChamberCode\": \"S\", " +
                "\"title\": \"Bill Title 1\", \"type\": \"SRES\" } " +
                "] }";
        // Enqueue a valid response and then an empty response to end pagination.
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonPage1)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("")  // empty response to signal no more records
                .addHeader("Content-Type", "application/json"));

        // Stub processor behavior: when given jsonPage1, return a list with one Bill.
        Bill bill = new Bill();
        bill.setBillNo("40");
        bill.setCongress("119");
        bill.setBillTitle("Bill Title 1");
        bill.setBillType("SRES");
        bill.setOriginChamber("Senate");
        bill.setOriginChamberCd("S");
        // For simplicity, assume latestActionDt is parsed to 2025-01-27.
        bill.setLatestActionDt(java.time.LocalDate.of(2025, 1, 27));
        bill.setLatestActionTxt("Action text 1.");
        when(recentBillProcessor.processRecentBills(anyString()))
                .thenReturn(Collections.singletonList(bill));

        // Call the scheduled service method (ignoring the @Scheduled annotation)
        scheduledService.loadRecentBills();

        // Verify that the WebClient made at least one GET request.
        RecordedRequest request1 = mockWebServer.takeRequest();
        assertEquals("GET", request1.getMethod());
        assertTrue(request1.getPath().contains("bill"));

        // Verify that billRepository.saveAll() was called with a list containing the processed bill.
        ArgumentCaptor<List<Bill>> billsCaptor = ArgumentCaptor.forClass(List.class);
        verify(billRepository, times(1)).saveAll(billsCaptor.capture());
        List<Bill> savedBills = billsCaptor.getValue();
        assertNotNull(savedBills);
        assertEquals(1, savedBills.size());
        assertEquals("40", savedBills.get(0).getBillNo());

        // Verify that hydrationRunner.runBillHydration() was called.
        verify(hydrationRunner, times(1)).runBillHydration(any(Instant.class));
    }

    /**
     * Test that if an exception occurs during processing, the exception propagates.
     */
    @Test
    void testLoadRecentBillsexceptionDuringProcessing() throws Exception {
        String json = "{ \"bills\": [ { \"congress\": \"119\", \"latestAction\": { \"actionDate\": \"2025-01-27\", \"text\": \"Action text.\" }, " +
                "\"number\": \"40\", \"originChamber\": \"Senate\", \"originChamberCode\": \"S\", " +
                "\"title\": \"Bill Title\", \"type\": \"SRES\" } ] }";
        // Enqueue a valid response.
        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json"));

        // Stub processor to throw an exception.
        when(recentBillProcessor.processRecentBills(anyString()))
                .thenThrow(new RuntimeException("Processing error"));

        // Expect the scheduled service to throw an exception.
        RuntimeException ex = assertThrows(RuntimeException.class, () -> scheduledService.loadRecentBills());
        assertEquals("Processing error", ex.getMessage());

        // Verify that no bills are saved and hydration is not invoked.
        verify(billRepository, never()).saveAll(any());
        verify(hydrationRunner, never()).runBillHydration(any());
    }
}
