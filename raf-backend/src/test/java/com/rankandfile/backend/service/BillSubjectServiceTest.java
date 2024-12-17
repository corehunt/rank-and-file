package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillSubjectProcessor;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.service.external.bill.BillSubjectService;
import jakarta.persistence.EntityNotFoundException;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for BillSubjectService.
 *
 * This class tests the BillSubjectService's methods to ensure they behave as expected
 * under various scenarios, including successful processing, handling of not found entities,
 * empty or null responses, and internal server errors.
 */
class BillSubjectServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillSubjectProcessor billSubjectProcessor;

    private WebClient webClient;

    private BillSubjectService billSubjectService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Initialize MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // Configure WebClient to point to MockWebServer
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        // Initialize BillSubjectService with mocked dependencies
        billSubjectService = new BillSubjectService(webClient, billSubjectProcessor, billRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    /**
     * Test the successful fetching and saving of bill subjects.
     *
     * This test ensures that when a valid request is made and the external API returns
     * a proper response, the service processes the response correctly and saves the
     * legislative subjects to the repository.
     */
    @Test
    void testFetchBillSubjectsSuccessfulRetrieval() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String apiResponse = "Anniversaries|Armed Forces and National Security|Army";

        // Create a mock Bill entity
        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill entity
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType))
                .thenReturn(bill);

        // Enqueue a successful mock response from the external API
        mockWebServer.enqueue(new MockResponse()
                .setBody(apiResponse)
                .addHeader("Content-Type", "text/plain"));

        // Mock the BillSubjectProcessor to process the API response
        when(billSubjectProcessor.processLegislativeSubjects(apiResponse, bill.getBillId()))
                .thenReturn(apiResponse);

        // Execute the service method
        billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);

        // Verify that the processor was called correctly
        verify(billSubjectProcessor, times(1)).processLegislativeSubjects(apiResponse, bill.getBillId());

        // Verify that the bill was updated with the legislative subjects
        assertEquals(apiResponse, bill.getLegislativeSubjects());

        // Verify that the billRepository.save was called with the updated bill
        verify(billRepository, times(1)).save(bill);

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNumber + "/subjects",
                recordedRequest.getPath());
    }

    /**
     * Test fetching bill subjects when the bill is not found.
     *
     * This test ensures that if the specified bill does not exist in the repository,
     * the service throws an EntityNotFoundException.
     */
    @Test
    void testFetchBillSubjectsBillNotFound() {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999"; // Assuming this bill number does not exist

        // Mock billRepository to return null, simulating bill not found
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType))
                .thenReturn(null);

        // Execute the service method and expect an EntityNotFoundException
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);
        });

        // Verify the exception message
        assertEquals("Bill not found", exception.getMessage());

        // Verify that the processor and repository save methods were never called
        verify(billSubjectProcessor, never()).processLegislativeSubjects(anyString(), anyString());
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that no external API call was made
        assertEquals(0, mockWebServer.getRequestCount(), "External API should not be called when bill is not found");
    }

    /**
     * Test fetching bill subjects when the external API returns an empty response.
     *
     * This test ensures that if the external API returns an empty response, the service
     * does not attempt to process or save any legislative subjects.
     */
    @Test
    void testFetchBillSubjectsEmptyResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String apiResponse = ""; // Empty response

        // Create a mock Bill entity
        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill entity
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType))
                .thenReturn(bill);

        // Enqueue an empty mock response from the external API
        mockWebServer.enqueue(new MockResponse()
                .setBody(apiResponse)
                .addHeader("Content-Type", "text/plain"));

        // Execute the service method
        billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);

        // Verify that the processor was not called
        verify(billSubjectProcessor, never()).processLegislativeSubjects(anyString(), anyString());

        // Verify that the bill's legislative subjects remain unchanged
        assertNull(bill.getLegislativeSubjects());

        // Verify that the billRepository.save was called with the bill (even if no changes were made)
        verify(billRepository, times(0)).save(bill);

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNumber + "/subjects",
                recordedRequest.getPath());
    }

    /**
     * Test fetching bill subjects when the external API returns a null response.
     *
     * This test ensures that if the external API returns a null response, the service
     * does not attempt to process or save any legislative subjects.
     */
    @Test
    void testFetchBillSubjectsNullResponse() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String apiResponse = null;

        // Create a mock Bill entity
        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill entity
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType))
                .thenReturn(bill);

        // Enqueue a mock response with no body (simulating null)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "text/plain"));

        // Execute the service method
        billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);

        // Verify that the processor was not called
        verify(billSubjectProcessor, never()).processLegislativeSubjects(anyString(), anyString());

        // Verify that the bill's legislative subjects remain unchanged
        assertNull(bill.getLegislativeSubjects());

        // Verify that the billRepository.save was called with the bill (even if no changes were made)
        verify(billRepository, times(0)).save(bill);

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNumber + "/subjects",
                recordedRequest.getPath());
    }

    /**
     * Test fetching bill subjects when an exception occurs during the external API call.
     *
     * This test ensures that if the external API call fails (e.g., network error, server error),
     * the service correctly logs the error and propagates the exception.
     */
    @Test
    void testFetchBillSubjectsExceptionDuringFetch() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        // Create a mock Bill entity
        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill entity
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType))
                .thenReturn(bill);

        // Enqueue a mock response with an error status code
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "text/plain"));

        // Execute the service method and expect a WebClientResponseException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);
        });

        // Verify that the exception message contains relevant information
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("500"));

        // Verify that the processor was not called
        verify(billSubjectProcessor, never()).processLegislativeSubjects(anyString(), anyString());

        // Verify that the bill's legislative subjects remain unchanged
        assertNull(bill.getLegislativeSubjects());

        // Verify that the billRepository.save was not called due to the exception
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNumber + "/subjects",
                recordedRequest.getPath());
    }

    /**
     * Test fetching bill subjects when an exception occurs during processing.
     *
     * This test ensures that if the BillSubjectProcessor throws an exception while processing
     * the API response, the service correctly propagates the exception and does not save
     * any legislative subjects.
     */
    @Test
    void testFetchBillSubjectsExceptionDuringProcessing() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";
        String apiResponse = "Anniversaries|Armed Forces and National Security|Army";

        // Create a mock Bill entity
        Bill bill = new Bill();
        bill.setBillId("hr3076-117");
        bill.setBillNo(billNumber);
        bill.setBillType(billType);

        // Mock billRepository to return the Bill entity
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType))
                .thenReturn(bill);

        // Enqueue a successful mock response from the external API
        mockWebServer.enqueue(new MockResponse()
                .setBody(apiResponse)
                .addHeader("Content-Type", "text/plain"));

        // Mock the BillSubjectProcessor to throw an exception during processing
        when(billSubjectProcessor.processLegislativeSubjects(apiResponse, bill.getBillId()))
                .thenThrow(new RuntimeException("Processing error"));

        // Execute the service method and expect a RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);
        });

        // Verify the exception message
        assertEquals("Processing error", exception.getMessage());

        // Verify that the bill's legislative subjects remain unchanged
        assertNull(bill.getLegislativeSubjects());

        // Verify that the billRepository.save was not called due to the exception
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the processor was called correctly
        verify(billSubjectProcessor, times(1)).processLegislativeSubjects(apiResponse, bill.getBillId());

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNumber + "/subjects",
                recordedRequest.getPath());
    }
}
