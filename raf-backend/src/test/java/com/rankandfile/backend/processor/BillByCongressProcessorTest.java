package com.rankandfile.backend.processor;

import com.google.gson.JsonSyntaxException;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillByCongressProcessorTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillByCongressProcessor billByCongressProcessor;

    @Test
    void testProcessBillList() {
        // Mocking the ID generation
        when(idGenerator.generateBillId(anyInt(), anyInt())).thenAnswer(invocation ->
                invocation.getArgument(0) + "-" + invocation.getArgument(1)
        );

        // Mocking the BillRepository to return no existing bills
        when(billRepository.findByCongressInAndBillNoIn(anySet(), anySet())).thenReturn(Collections.emptyList());

        // Test JSON response
        String json = "{\n" +
                "    \"bills\": [\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": {\n" +
                "                \"actionDate\": \"2024-08-02\",\n" +
                "                \"text\": \"Message on Senate action sent to the House.\"\n" +
                "            },\n" +
                "            \"number\": \"4367\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"originChamberCode\": \"S\",\n" +
                "            \"title\": \"Thomas R. Carper Water Resources Development Act of 2024\",\n" +
                "            \"type\": \"S\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": {\n" +
                "                \"actionDate\": \"2024-08-02\",\n" +
                "                \"text\": \"Message on Senate action sent to the House.\"\n" +
                "            },\n" +
                "            \"number\": \"4235\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"originChamberCode\": \"S\",\n" +
                "            \"title\": \"Reauthorizing Support and Treatment for Officers in Crisis Act of 2024\",\n" +
                "            \"type\": \"S\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": {\n" +
                "                \"actionDate\": \"2024-08-02\",\n" +
                "                \"text\": \"Message on Senate action sent to the House.\"\n" +
                "            },\n" +
                "            \"number\": \"4199\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"originChamberCode\": \"S\",\n" +
                "            \"title\": \"JUDGES Act of 2024\",\n" +
                "            \"type\": \"S\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);
        assertEquals(3, finalBillList.size());

        // Assert the content of the first bill
        Bill bill1 = finalBillList.stream().filter(b -> b.getBillNo() == 4367).findFirst().orElse(null);
        assertNotNull(bill1);
        assertEquals(118, bill1.getCongress());
        assertEquals(4367, bill1.getBillNo());
        assertEquals("Thomas R. Carper Water Resources Development Act of 2024", bill1.getBillTitle());
        assertEquals("S", bill1.getOriginChamberCd());
        assertEquals("Senate", bill1.getOriginChamber());
        assertEquals("S", bill1.getBillType());
        assertEquals(LocalDate.of(2024, 8, 2), bill1.getLatestActionDt());
        assertEquals("Message on Senate action sent to the House.", bill1.getLatestActionTxt());

        // Verify that the IdGenerator method was called the correct number of times
        verify(idGenerator, times(3)).generateBillId(anyInt(), anyInt());
    }

    @Test
    public void testProcessEmptyBillList() {
        String json = "{ \"bills\": [] }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(0, finalBillList.size());
    }

    @Test
    public void testProcessBillWithMissingFields() {
        String json = "{ \"bills\": [ { \"congress\": 118, \"number\": \"1234\" } ] }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(1, finalBillList.size());
        Bill bill = finalBillList.get(0);
        assertEquals(118, bill.getCongress());
        assertEquals(1234, bill.getBillNo());
        assertNull(bill.getBillTitle()); // Title is missing in JSON
        assertNull(bill.getLatestActionDt()); // Latest action date is missing
        assertNull(bill.getLatestActionTxt()); // Latest action text is missing
    }

    @Test
    public void testProcessMultipleBillsWithDifferentData() {
        String json = "{\n" +
                "    \"bills\": [\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": { \"actionDate\": \"2024-08-02\", \"text\": \"Message on Senate action sent to the House.\" },\n" +
                "            \"number\": \"4367\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"originChamberCode\": \"S\",\n" +
                "            \"title\": \"Thomas R. Carper Water Resources Development Act of 2024\",\n" +
                "            \"type\": \"S\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": { \"actionDate\": \"2024-07-15\", \"text\": \"Referred to Committee.\" },\n" +
                "            \"number\": \"1234\",\n" +
                "            \"originChamber\": \"House\",\n" +
                "            \"originChamberCode\": \"H\",\n" +
                "            \"title\": \"A Sample Bill for Testing\",\n" +
                "            \"type\": \"HR\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(2, finalBillList.size());

        // First Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 4367 &&
                "Thomas R. Carper Water Resources Development Act of 2024".equals(bill.getBillTitle()) &&
                "Message on Senate action sent to the House.".equals(bill.getLatestActionTxt()) &&
                LocalDate.of(2024, 8, 2).equals(bill.getLatestActionDt())));

        // Second Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 1234 &&
                "A Sample Bill for Testing".equals(bill.getBillTitle()) &&
                "Referred to Committee.".equals(bill.getLatestActionTxt()) &&
                LocalDate.of(2024, 7, 15).equals(bill.getLatestActionDt())));
    }

    @Test
    public void testProcessNullBillList() {
        String json = "{ \"bills\": null }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(0, finalBillList.size());
    }

    @Test
    public void testProcessMixedValidAndInvalidBills() {
        String json = "{\n" +
                "    \"bills\": [\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": { \"actionDate\": \"2024-08-02\", \"text\": \"Message on Senate action sent to the House.\" },\n" +
                "            \"number\": \"4367\",\n" +
                "            \"originChamber\": \"Senate\",\n" +
                "            \"originChamberCode\": \"S\",\n" +
                "            \"title\": \"Thomas R. Carper Water Resources Development Act of 2024\",\n" +
                "            \"type\": \"S\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"congress\": null,\n" +
                "            \"number\": null\n" + // Invalid data (missing congress and number)
                "        }\n" +
                "    ]\n" +
                "}";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(1, finalBillList.size());

        // Valid Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 4367 &&
                "Thomas R. Carper Water Resources Development Act of 2024".equals(bill.getBillTitle())));
    }

    @Test
    public void testProcessBillListWithExistingBills() {
        String json = "{\n" +
                "    \"bills\": [\n" +
                "        {\n" +
                "            \"congress\": 118,\n" +
                "            \"latestAction\": { \"actionDate\": \"2024-08-02\", \"text\": \"Updated Text.\" },\n" +
                "            \"number\": \"4367\",\n" +
                "            \"title\": \"Updated Title\",\n" +
                "            \"type\": \"S\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Bill existingBill = new Bill();
        existingBill.setBillId("118-4367");
        existingBill.setCongress(118);
        existingBill.setBillNo(4367);
        existingBill.setBillTitle("Original Title");
        existingBill.setLatestActionDt(LocalDate.of(2024, 8, 1));
        existingBill.setLatestActionTxt("Original Text");

        when(billRepository.findByCongressInAndBillNoIn(anySet(), anySet()))
                .thenReturn(Collections.singletonList(existingBill));

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(1, finalBillList.size());
        Bill bill = finalBillList.get(0);

        // Verify that the existing bill was updated
        assertEquals("Updated Title", bill.getBillTitle());
        assertEquals(LocalDate.of(2024, 8, 2), bill.getLatestActionDt()); // Updated date
        assertEquals("Updated Text.", bill.getLatestActionTxt());

        // Verify that IdGenerator.generateBillId() was not called for existing bill
        verify(idGenerator, never()).generateBillId(anyInt(), anyInt());
    }

    @Test
    public void testProcessBillListWithInvalidDate() {
        String json = "{ \"bills\": [ { \"congress\": 118, \"number\": \"1234\", \"latestAction\": { \"actionDate\": \"invalid-date\", \"text\": \"Invalid date action\" } } ] }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(1, finalBillList.size());
        Bill bill = finalBillList.get(0);
        assertNull(bill.getLatestActionDt()); // Date parsing failed
        assertEquals("Invalid date action", bill.getLatestActionTxt());
    }

    @Test
    public void testProcessBillListWithMissingCongressOrNumber() {
        String json = "{ \"bills\": [ { \"title\": \"Missing Congress and Number\" } ] }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(0, finalBillList.size()); // Bill should be skipped
    }

    @Test
    public void testProcessBillListWithEmptyJson() {
        String json = "{}";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(0, finalBillList.size());
    }

    @Test
    public void testProcessBillListWithNullJson() {
        String json = null;

        assertThrows(NullPointerException.class, () -> billByCongressProcessor.processBillList(json));
    }

    @Test
    public void testProcessBillListWithEmptyBillsArray() {
        String json = "{ \"bills\": [] }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(0, finalBillList.size());
    }

    @Test
    public void testProcessBillListWithMalformedJson() {
        String json = "{ \"bills\": [ { \"congress\": 118, \"number\": \"1234\", ";

        assertThrows(JsonSyntaxException.class, () -> billByCongressProcessor.processBillList(json));
    }
}
