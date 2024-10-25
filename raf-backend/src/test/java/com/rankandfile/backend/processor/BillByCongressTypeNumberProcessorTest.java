package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillByCongressTypeNumberProcessorTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    @Test
    public void testProcessBillWithAllFields() {
        String json = getValidBillJson();

        // Existing bill
        Bill mockBill = new Bill();
        mockBill.setBillId("118-6937");
        mockBill.setCongress(118);
        mockBill.setBillNo(6937);

        when(billRepository.findByCongressAndBillNo(118, 6937)).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNotNull(processedBill);
        assertEquals("118-6937", processedBill.getBillId());
        assertEquals(6937, processedBill.getBillNo());
        assertEquals("The Organic Dairy Data Collection Act", processedBill.getBillTitle());
        assertEquals(LocalDate.of(2024, 1, 10), processedBill.getIntroducedDt());
        assertEquals(LocalDate.of(2024, 1, 10), processedBill.getLatestActionDt());
        assertEquals("Referred to the House Committee on Agriculture.", processedBill.getLatestActionTxt());
        assertEquals("Agriculture and Food", processedBill.getPolicyArea());
        assertEquals(118, processedBill.getCongress());
        assertEquals("HR", processedBill.getBillType());
        assertEquals("House", processedBill.getOriginChamber());
        assertEquals("H", processedBill.getOriginChamberCd());
    }

    @Test
    public void testProcessNewBill() {
        String json = getValidBillJson();

        // No existing bill in the repository
        when(billRepository.findByCongressAndBillNo(118, 6937)).thenReturn(null);
        when(idGenerator.generateBillId(118, 6937)).thenReturn("118-6937");

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNotNull(processedBill);
        assertEquals("118-6937", processedBill.getBillId());
        assertEquals(6937, processedBill.getBillNo());

        verify(idGenerator, times(1)).generateBillId(118, 6937);
    }

    @Test
    public void testProcessBillWithMissingFields() {
        String json = "{ \"bill\": { \"congress\": 118, \"number\": \"6937\" } }";

        Bill mockBill = new Bill();
        mockBill.setBillId("118-6937");
        mockBill.setCongress(118);
        mockBill.setBillNo(6937);

        when(billRepository.findByCongressAndBillNo(118, 6937)).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNotNull(processedBill);
        assertEquals(118, processedBill.getCongress());
        assertEquals(6937, processedBill.getBillNo());
        assertNull(processedBill.getBillTitle());
        assertNull(processedBill.getIntroducedDt());
    }

    @Test
    public void testProcessBillWithInvalidDate() {
        String json = "{ \"bill\": { \"congress\": 118, \"number\": \"6937\", \"introducedDate\": \"invalid-date\" } }";

        Bill mockBill = new Bill();
        mockBill.setBillId("118-6937");
        mockBill.setCongress(118);
        mockBill.setBillNo(6937);

        when(billRepository.findByCongressAndBillNo(118, 6937)).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNotNull(processedBill);
        assertNull(processedBill.getIntroducedDt());
    }

    @Test
    public void testProcessBillWithInvalidJson() {
        String json = "{ \"bill\": ";

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNull(processedBill);
    }

    @Test
    public void testProcessBillWithMissingBillObject() {
        String json = "{ }";

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNull(processedBill);
    }

    @Test
    public void testProcessBillWithNullJson() {
        String json = null;

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNull(processedBill);
    }

    @Test
    public void testProcessBillWithMissingCongressOrNumber() {
        String json = "{ \"bill\": { \"title\": \"Missing Congress and Number\" } }";

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNull(processedBill);
    }

    @Test
    public void testProcessBillWithLaws() {
        String json = getBillJsonWithLaws();

        // Existing bill
        Bill mockBill = new Bill();
        mockBill.setBillId("117-3076");
        mockBill.setCongress(117);
        mockBill.setBillNo(3076);

        when(billRepository.findByCongressAndBillNo(117, 3076)).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNotNull(processedBill);
        assertEquals("117-3076", processedBill.getBillId());
        assertEquals(3076, processedBill.getBillNo());
        assertEquals("Postal Service Reform Act of 2022", processedBill.getBillTitle());
        assertEquals("117-108", processedBill.getLawNo());
        assertEquals("Public Law", processedBill.getLawType());
        assertEquals("Y", processedBill.getIsLawFl());
    }

    @Test
    public void testProcessBillWithoutLaws() {
        String json = getValidBillJson();

        // Existing bill
        Bill mockBill = new Bill();
        mockBill.setBillId("118-6937");
        mockBill.setCongress(118);
        mockBill.setBillNo(6937);
        mockBill.setLawNo("Some Law No");
        mockBill.setLawType("Some Law Type");
        mockBill.setIsLawFl("Y");

        when(billRepository.findByCongressAndBillNo(118, 6937)).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertNotNull(processedBill);
        assertEquals("118-6937", processedBill.getBillId());
        assertNull(processedBill.getLawNo());
        assertNull(processedBill.getLawType());
        assertNull(processedBill.getIsLawFl());
    }

    // Helper method to get valid JSON
    private String getValidBillJson() {
        return "{\n" +
                "    \"bill\": {\n" +
                "        \"congress\": 118,\n" +
                "        \"number\": \"6937\",\n" +
                "        \"title\": \"The Organic Dairy Data Collection Act\",\n" +
                "        \"introducedDate\": \"2024-01-10\",\n" +
                "        \"latestAction\": {\n" +
                "            \"actionDate\": \"2024-01-10\",\n" +
                "            \"text\": \"Referred to the House Committee on Agriculture.\"\n" +
                "        },\n" +
                "        \"policyArea\": {\n" +
                "            \"name\": \"Agriculture and Food\"\n" +
                "        },\n" +
                "        \"originChamber\": \"House\",\n" +
                "        \"originChamberCode\": \"H\",\n" +
                "        \"type\": \"HR\"\n" +
                "    }\n" +
                "}";
    }

    private String getBillJsonWithLaws() {
        return "{\n" +
                "    \"bill\": {\n" +
                "        \"congress\": 117,\n" +
                "        \"number\": \"3076\",\n" +
                "        \"title\": \"Postal Service Reform Act of 2022\",\n" +
                "        \"laws\": [\n" +
                "            {\n" +
                "                \"number\": \"117-108\",\n" +
                "                \"type\": \"Public Law\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
    }
}
