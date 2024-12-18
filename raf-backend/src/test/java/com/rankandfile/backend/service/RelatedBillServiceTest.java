package com.rankandfile.backend.service;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.RelatedBillProcessor;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.service.external.bill.RelatedBillService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for RelatedBillService.
 *
 * This class tests the RelatedBillService's methods to ensure they behave as expected
 * under various scenarios, including successful processing, handling of not found entities,
 * empty or null responses, and error conditions.
 */
@ExtendWith(MockitoExtension.class)
class RelatedBillServiceTest {

    private MockWebServer mockWebServer;

    @Mock
    private BillRepository billRepository;

    @Mock
    private RelatedBillProcessor relatedBillProcessor;

    private WebClient webClient;

    @InjectMocks
    private RelatedBillService relatedBillService;

    private Bill mainBill;

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

        // Initialize RelatedBillService with mocked dependencies
        relatedBillService = new RelatedBillService(webClient, billRepository, relatedBillProcessor);

        // Initialize the main bill
        mainBill = new Bill();
        mainBill.setBillId("hr3076-117");
        mainBill.setBillNo("3076");
        mainBill.setBillType("HR");
        mainBill.setCongress("117");
        mainBill.setBillTitle("Sample Bill Title");
        mainBill.setOriginChamber("House");
        mainBill.setIntroducedDt(LocalDate.of(2021, 3, 15));
        mainBill.setRelatedBills(new java.util.HashSet<>());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    /**
     * Test the successful fetching and saving of related bills.
     *
     * This test ensures that when a valid request is made and the external API returns
     * a proper response, the service processes the response correctly and saves the
     * related bills to the repository.
     */
    @Test
    void testGetRelatedBillsSuccessfulRetrieval() throws Exception {
        String congressNo = "118";
        String billType = "s";
        String billNo = "3740";
        String apiResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"S\",\n" +
                "            \"number\": \"3740\",\n" +
                "            \"title\": \"STRONGER Act\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"introducedDt\": \"2024-01-15\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"HR\",\n" +
                "            \"number\": \"238\",\n" +
                "            \"title\": \"Another Act\",\n" +
                "            \"originChamber\": \"House\",\n" +
                "            \"introducedDt\": \"2024-02-20\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock the external API response
        mockWebServer.enqueue(new MockResponse()
                .setBody(apiResponse)
                .addHeader("Content-Type", "application/json"));

        // Mock billRepository to return the main bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(mainBill);

        // Mock the processor to process the API response and update the main bill
        doAnswer(invocation -> {
            String response = invocation.getArgument(0);
            Bill bill = invocation.getArgument(1);

            // Simulate processing: parse the JSON and add related bills
            // Here, manually adding two related bills for simplicity
            Bill relatedBill1 = new Bill();
            relatedBill1.setBillId("s3740-118");
            relatedBill1.setBillNo("3740");
            relatedBill1.setBillType("S");
            relatedBill1.setCongress("118");
            relatedBill1.setBillTitle("STRONGER Act");
            relatedBill1.setOriginChamber("Senate");
            relatedBill1.setIntroducedDt(LocalDate.of(2024, 1, 15));

            Bill relatedBill2 = new Bill();
            relatedBill2.setBillId("hr238-118");
            relatedBill2.setBillNo("238");
            relatedBill2.setBillType("HR");
            relatedBill2.setCongress("118");
            relatedBill2.setBillTitle("Another Act");
            relatedBill2.setOriginChamber("House");
            relatedBill2.setIntroducedDt(LocalDate.of(2024, 2, 20));

            Set<Bill> relatedBills = new HashSet<>();
            relatedBills.add(relatedBill1);
            relatedBills.add(relatedBill2);

            bill.setRelatedBills(relatedBills);

            return bill;
        }).when(relatedBillProcessor).processRelatedBills(apiResponse, mainBill);

        // Execute the service method
        relatedBillService.getRelatedBills(congressNo, billType, billNo);

        // Verify that the processor was called correctly
        verify(relatedBillProcessor, times(1)).processRelatedBills(apiResponse, mainBill);

        // Verify that the main bill's relatedBills set has been updated
        assertNotNull(mainBill.getRelatedBills(), "Related bills should not be null");
        assertEquals(2, mainBill.getRelatedBills().size(), "There should be two related bills");

        // Verify that the billRepository.save was called with the updated main bill
        verify(billRepository, times(1)).save(mainBill);

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod(), "HTTP method should be GET");
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNo + "/relatedbills",
                recordedRequest.getPath(), "API endpoint path mismatch");
    }

    /**
     * Test loading related bills when the bill is not found in the repository.
     *
     * This test ensures that if the specified bill does not exist, the service logs a warning
     * and does not attempt to fetch or process related bills.
     */
    @Test
    void testGetRelatedBillsBillNotFound() {
        String congressNo = "117";
        String billType = "hr";
        String billNo = "9999"; // Assuming this bill number does not exist

        // Mock billRepository to return null, simulating bill not found
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(null);

        // Execute the service method
        relatedBillService.getRelatedBills(congressNo, billType, billNo);

        // Verify that the processor and repository save were never called
        verify(relatedBillProcessor, never()).processRelatedBills(anyString(), any(Bill.class));
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the external API was never called
        assertEquals(0, mockWebServer.getRequestCount(), "External API should not be called when bill is not found");
    }

    /**
     * Test loading related bills when the external API returns an empty response.
     *
     * This test ensures that if the external API returns an empty response, the service
     * does not attempt to process or save any related bills.
     */
    @Test
    void testGetRelatedBillsEmptyApiResponse() throws Exception {
        String congressNo = "118";
        String billType = "s";
        String billNo = "3740";
        String apiResponse = ""; // Empty response

        // Mock billRepository to return the main bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(mainBill);

        // Enqueue an empty mock response from the external API
        mockWebServer.enqueue(new MockResponse()
                .setBody(apiResponse)
                .addHeader("Content-Type", "application/json"));

        // Execute the service method
        relatedBillService.getRelatedBills(congressNo, billType, billNo);

        // Verify that the processor was not called due to empty response
        verify(relatedBillProcessor, never()).processRelatedBills(anyString(), any(Bill.class));

        // Verify that the billRepository.save was not called since no related bills were processed
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod(), "HTTP method should be GET");
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNo + "/relatedbills",
                recordedRequest.getPath(), "API endpoint path mismatch");
    }

    /**
     * Test loading related bills when the external API call results in a server error.
     *
     * This test ensures that if the external API returns an error status code, the service
     * handles it appropriately by logging the error and propagating the exception.
     */
    @Test
    void testGetRelatedBillsExternalApiServerError() throws Exception {
        String congressNo = "118";
        String billType = "s";
        String billNo = "3740";
        String apiResponse = "Internal Server Error";

        // Mock billRepository to return the main bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(mainBill);

        // Enqueue a server error mock response from the external API
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(apiResponse)
                .addHeader("Content-Type", "text/plain"));

        // Execute the service method and expect a WebClientResponseException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            relatedBillService.getRelatedBills(congressNo, billType, billNo);
        });

        // Verify that the exception message contains relevant information
        assertTrue(exception.getMessage().contains("500"));

        // Verify that the processor was not called due to the server error
        verify(relatedBillProcessor, never()).processRelatedBills(anyString(), any(Bill.class));

        // Verify that the billRepository.save was not called due to the exception
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod(), "HTTP method should be GET");
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNo + "/relatedbills",
                recordedRequest.getPath(), "API endpoint path mismatch");
    }

    /**
     * Test loading related bills when the RelatedBillProcessor throws an exception during processing.
     *
     * This test ensures that if the processor encounters an error, the service handles it appropriately
     * by not saving the bill and propagating the exception.
     */
    @Test
    void testGetRelatedBillsProcessorThrowsException() throws Exception {
        String congressNo = "118";
        String billType = "s";
        String billNo = "3740";
        String apiResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"S\",\n" +
                "            \"number\": \"3740\",\n" +
                "            \"title\": \"STRONGER Act\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"introducedDt\": \"2024-01-15\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock the external API response
        mockWebServer.enqueue(new MockResponse()
                .setBody(apiResponse)
                .addHeader("Content-Type", "application/json"));

        // Mock billRepository to return the main bill
        when(billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType))
                .thenReturn(mainBill);

        // Mock the processor to throw an exception during processing
        doThrow(new RuntimeException("Processing error"))
                .when(relatedBillProcessor).processRelatedBills(apiResponse, mainBill);

        // Execute the service method and expect a RuntimeException
        Exception exception = assertThrows(RuntimeException.class, () -> {
            relatedBillService.getRelatedBills(congressNo, billType, billNo);
        });

        // Verify that the exception message contains relevant information
        assertEquals("Processing error", exception.getMessage());

        // Verify that the processor was called correctly
        verify(relatedBillProcessor, times(1)).processRelatedBills(apiResponse, mainBill);

        // Verify that the billRepository.save was not called due to the exception
        verify(billRepository, never()).save(any(Bill.class));

        // Verify that the external API was called with the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(recordedRequest, "External API was not called");
        assertEquals("GET", recordedRequest.getMethod(), "HTTP method should be GET");
        assertEquals("/bill/" + congressNo + "/" + billType.toLowerCase() + "/" + billNo + "/relatedbills",
                recordedRequest.getPath(), "API endpoint path mismatch");
    }
}
