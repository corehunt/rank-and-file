package com.rankandfile.dataloader.processor;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

/**
 * Test class for RelatedBillProcessor.
 *
 * This class tests the RelatedBillProcessor's methods to ensure they behave as expected
 * under various scenarios, including successful processing, handling of missing or
 * invalid data, and error conditions.
 */
@ExtendWith(MockitoExtension.class)
class RelatedBillProcessorTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private RelatedBillProcessor processor;

    private Bill mainBill;

    @BeforeEach
    void setUp() {
        // Initialize the main bill
        mainBill = new Bill();
        mainBill.setBillId("hr3076-117");
        mainBill.setBillNo("3076");
        mainBill.setBillType("HR");
        mainBill.setCongress("117");
        mainBill.setBillTitle("Sample Bill Title");
        mainBill.setOriginChamber("House");
        mainBill.setIntroducedDt(LocalDate.of(2021, 3, 15));
        mainBill.setRelatedBills(new HashSet<>());
    }

    /**
     * Test processing of related bills with a valid JSON response.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsSuccessful() {
        String jsonResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"S\",\n" +
                "            \"number\": \"3740\",\n" +
                "            \"title\": \"STRONGER Act\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"HR\",\n" +
                "            \"number\": \"238\",\n" +
                "            \"title\": \"Another Act\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock related bills returned by the repository
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

        // Mock repository behavior
        when(billRepository.findByCongressAndBillNoAndBillType("118", "3740", "S")).thenReturn(relatedBill1);
        when(billRepository.findByCongressAndBillNoAndBillType("118", "238", "HR")).thenReturn(relatedBill2);

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill.getRelatedBills(), "Related bills should not be null");
        assertEquals(2, updatedBill.getRelatedBills().size(), "There should be two related bills");

        Set<Bill> expectedRelatedBills = new HashSet<>();
        expectedRelatedBills.add(relatedBill1);
        expectedRelatedBills.add(relatedBill2);

        assertEquals(expectedRelatedBills, updatedBill.getRelatedBills(), "Related bills should match the expected set");

        // Verify repository interactions
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "3740", "S");
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "238", "HR");
    }

    /**
     * Test processing when the relatedBills field is missing from the JSON.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsMissingField() {
        String jsonResponse = "{\n" +
                "    \"pagination\": {\n" +
                "        \"count\": 1\n" +
                "    },\n" +
                "    \"request\": {\n" +
                "        \"billNumber\": \"238\",\n" +
                "        \"billType\": \"hr\",\n" +
                "        \"billUrl\": \"https://api.congress.gov/v3/bill/118/hr/238?format=json\",\n" +
                "        \"congress\": \"118\",\n" +
                "        \"contentType\": \"application/json\",\n" +
                "        \"format\": \"json\"\n" +
                "    }\n" +
                "}";

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill, "Bill should not be null");
        assertTrue(updatedBill.getRelatedBills().isEmpty(), "Related bills should be empty");
        assertTrue(mainBill.getRelatedBills().isEmpty(), "Main bill's related bills should remain empty");

        // Verify repository interactions
        verifyNoInteractions(billRepository);
    }

    /**
     * Test processing when the relatedBills array is empty.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsEmptyArray() {
        String jsonResponse = "{\n" +
                "    \"relatedBills\": []\n" +
                "}";

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill, "Bill should not be null");
        assertTrue(updatedBill.getRelatedBills().isEmpty(), "Related bills should be empty");
        assertTrue(mainBill.getRelatedBills().isEmpty(), "Main bill's related bills should remain empty");

        // Verify repository interactions
        verifyNoInteractions(billRepository);
    }

    /**
     * Test processing with invalid JSON input.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsInvalidJson() {
        String jsonResponse = "Invalid JSON";

        // Execute the processor
        assertThrows(com.google.gson.JsonSyntaxException.class, () -> {
            processor.processRelatedBills(jsonResponse, mainBill);
        });

        // Verify repository interactions
        verifyNoInteractions(billRepository);
    }

    /**
     * Test processing when relatedBills contain bills that do not exist in the repository.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsBillsNotFound() {
        String jsonResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"S\",\n" +
                "            \"number\": \"9999\",\n" +
                "            \"title\": \"Non-Existent Act\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock repository to return null (bill not found)
        when(billRepository.findByCongressAndBillNoAndBillType("118", "9999", "S")).thenReturn(null);

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill, "Bill should not be null");
        assertTrue(updatedBill.getRelatedBills().isEmpty(), "Related bills should be empty since the related bill was not found");
        assertTrue(mainBill.getRelatedBills().isEmpty(), "Main bill's related bills should remain empty");

        // Verify repository interactions
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "9999", "S");
    }

    /**
     * Test processing with some related bills missing required fields.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsMissingFieldsInRelatedBill() {
        String jsonResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        {\n" +
                "            \"type\": \"S\",\n" +
                "            \"number\": \"3740\",\n" +
                "            \"title\": \"STRONGER Act\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"HR\",\n" +
                "            \"title\": \"Another Act\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"HR\",\n" +
                "            \"number\": \"238\",\n" +
                "            \"title\": \"Valid Act\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock repository behavior
        Bill relatedBill = new Bill();
        relatedBill.setBillId("hr238-118");
        relatedBill.setBillNo("238");
        relatedBill.setBillType("HR");
        relatedBill.setCongress("118");
        relatedBill.setBillTitle("Valid Act");
        relatedBill.setOriginChamber("House");
        relatedBill.setIntroducedDt(LocalDate.of(2024, 2, 20));

        when(billRepository.findByCongressAndBillNoAndBillType(null, "3740", "S")).thenReturn(null);
        when(billRepository.findByCongressAndBillNoAndBillType("118", null, "HR")).thenReturn(null);
        when(billRepository.findByCongressAndBillNoAndBillType("118", "238", "HR")).thenReturn(relatedBill);

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill, "Bill should not be null");
        assertEquals(1, updatedBill.getRelatedBills().size(), "Only one related bill should be added");

        Set<Bill> expectedRelatedBills = new HashSet<>();
        expectedRelatedBills.add(relatedBill);

        assertEquals(expectedRelatedBills, updatedBill.getRelatedBills(), "Related bills should contain only the valid related bill");

        // Verify repository interactions
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "238", "HR");
    }

    /**
     * Test processing with related bills having additional unexpected fields.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsWithAdditionalFields() {
        String jsonResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"S\",\n" +
                "            \"number\": \"3740\",\n" +
                "            \"title\": \"STRONGER Act\",\n" +
                "            \"extraField\": \"Extra Value\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"HR\",\n" +
                "            \"number\": \"238\",\n" +
                "            \"title\": \"Another Act\",\n" +
                "            \"anotherExtraField\": \"Another Extra Value\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock related bills returned by the repository
        Bill relatedBill1 = new Bill();
        relatedBill1.setBillId("s3740-118");
        relatedBill1.setBillNo("3740");
        relatedBill1.setBillType("S");
        relatedBill1.setCongress("118");
        relatedBill1.setBillNo("3740");
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

        // Mock repository behavior
        when(billRepository.findByCongressAndBillNoAndBillType("118", "3740", "S")).thenReturn(relatedBill1);
        when(billRepository.findByCongressAndBillNoAndBillType("118", "238", "HR")).thenReturn(relatedBill2);

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill.getRelatedBills(), "Related bills should not be null");
        assertEquals(2, updatedBill.getRelatedBills().size(), "There should be two related bills");

        Set<Bill> expectedRelatedBills = new HashSet<>();
        expectedRelatedBills.add(relatedBill1);
        expectedRelatedBills.add(relatedBill2);

        assertEquals(expectedRelatedBills, updatedBill.getRelatedBills(), "Related bills should match the expected set");

        // Verify repository interactions
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "3740", "S");
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "238", "HR");
    }

    /**
     * Test processing when the JSON is empty.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsEmptyJson() {
        String jsonResponse = "{}";

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill, "Bill should not be null");
        assertTrue(updatedBill.getRelatedBills().isEmpty(), "Related bills should be empty");
        assertTrue(mainBill.getRelatedBills().isEmpty(), "Main bill's related bills should remain empty");

        // Verify repository interactions
        verifyNoInteractions(billRepository);
    }

    /**
     * Test processing when the relatedBills array contains null elements.
     *
     * @throws Exception if any unexpected error occurs during processing.
     */
    @Test
    void testProcessRelatedBillsWithNullElements() {
        String jsonResponse = "{\n" +
                "    \"relatedBills\": [\n" +
                "        null,\n" +
                "        {\n" +
                "            \"congress\": \"118\",\n" +
                "            \"type\": \"HR\",\n" +
                "            \"number\": \"238\",\n" +
                "            \"title\": \"Another Act\"\n" +
                "        },\n" +
                "        null\n" +
                "    ]\n" +
                "}";

        // Mock related bills returned by the repository
        Bill relatedBill = new Bill();
        relatedBill.setBillId("hr238-118");
        relatedBill.setBillNo("238");
        relatedBill.setBillType("HR");
        relatedBill.setCongress("118");
        relatedBill.setBillNo("238");
        relatedBill.setBillTitle("Another Act");
        relatedBill.setOriginChamber("House");
        relatedBill.setIntroducedDt(LocalDate.of(2024, 2, 20));

        // Mock repository behavior
        when(billRepository.findByCongressAndBillNoAndBillType("118", "238", "HR")).thenReturn(relatedBill);

        // Execute the processor
        Bill updatedBill = processor.processRelatedBills(jsonResponse, mainBill);

        // Assertions
        assertNotNull(updatedBill.getRelatedBills(), "Related bills should not be null");
        assertEquals(1, updatedBill.getRelatedBills().size(), "There should be one related bill");

        Set<Bill> expectedRelatedBills = new HashSet<>();
        expectedRelatedBills.add(relatedBill);

        assertEquals(expectedRelatedBills, updatedBill.getRelatedBills(), "Related bills should contain only the valid related bill");

        // Verify repository interactions
        verify(billRepository, times(1)).findByCongressAndBillNoAndBillType("118", "238", "HR");
    }

}
