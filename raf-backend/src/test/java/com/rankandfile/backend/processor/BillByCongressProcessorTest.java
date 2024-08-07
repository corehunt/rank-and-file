package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.util.IdGenerator;
import com.rankandfile.backend.util.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillByCongressProcessorTest {

    private BillByCongressProcessor billByCongressProcessor;
    private IdGenerator idGenerator;
    private Supplier billSupplier;

    @BeforeEach
    void setUp() {
        idGenerator = mock(IdGenerator.class);
        billSupplier = mock(Supplier.class);
        billByCongressProcessor = new BillByCongressProcessor(idGenerator, billSupplier);
    }

    @Test
    void testProcessBillList() {
        // Mocking the ID generation
        when(idGenerator.generateBillId(anyInt(), anyInt())).thenAnswer(invocation ->
                "ID-" + invocation.getArgument(0) + "-" + invocation.getArgument(1)
        );

        // Mocking the Supplier to return a new Bill object when findOrCreateBill is called
        when(billSupplier.findOrCreateBill(anyInt(), anyInt())).thenAnswer(invocation -> new Bill());

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
                "            \"type\": \"S\",\n" +
                "            \"updateDate\": \"2024-08-03\",\n" +
                "            \"updateDateIncludingText\": \"2024-08-03T11:08:17Z\",\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/s/4367?format=json\"\n" +
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
                "            \"type\": \"S\",\n" +
                "            \"updateDate\": \"2024-08-03\",\n" +
                "            \"updateDateIncludingText\": \"2024-08-03T11:03:16Z\",\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/s/4235?format=json\"\n" +
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
                "            \"type\": \"S\",\n" +
                "            \"updateDate\": \"2024-08-03\",\n" +
                "            \"updateDateIncludingText\": \"2024-08-03T11:03:16Z\",\n" +
                "            \"url\": \"https://api.congress.gov/v3/bill/118/s/4199?format=json\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"pagination\": {\n" +
                "        \"count\": 16968,\n" +
                "        \"next\": \"https://api.congress.gov/v3/bill/118?offset=20&limit=20&format=json\"\n" +
                "    },\n" +
                "    \"request\": {\n" +
                "        \"congress\": \"118\",\n" +
                "        \"contentType\": \"application/json\",\n" +
                "        \"format\": \"json\"\n" +
                "    }\n" +
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
        assertEquals("2024-08-02", bill1.getLatestActionDt().toString());
        assertEquals("Message on Senate action sent to the House.", bill1.getLatestActionTxt());

        // Assert the content of the second bill
        Bill bill2 = finalBillList.stream().filter(b -> b.getBillNo() == 4235).findFirst().orElse(null);
        assertNotNull(bill2);
        assertEquals(118, bill2.getCongress());
        assertEquals(4235, bill2.getBillNo());
        assertEquals("Reauthorizing Support and Treatment for Officers in Crisis Act of 2024", bill2.getBillTitle());
        assertEquals("S", bill2.getOriginChamberCd());
        assertEquals("Senate", bill2.getOriginChamber());
        assertEquals("S", bill2.getBillType());
        assertEquals("2024-08-02", bill2.getLatestActionDt().toString());
        assertEquals("Message on Senate action sent to the House.", bill2.getLatestActionTxt());

        // Assert the content of the third bill
        Bill bill3 = finalBillList.stream().filter(b -> b.getBillNo() == 4199).findFirst().orElse(null);
        assertNotNull(bill3);
        assertEquals(118, bill3.getCongress());
        assertEquals(4199, bill3.getBillNo());
        assertEquals("JUDGES Act of 2024", bill3.getBillTitle());
        assertEquals("S", bill3.getOriginChamberCd());
        assertEquals("Senate", bill3.getOriginChamber());
        assertEquals("S", bill3.getBillType());
        assertEquals("2024-08-02", bill3.getLatestActionDt().toString());
        assertEquals("Message on Senate action sent to the House.", bill3.getLatestActionTxt());

        // Verify that the IdGenerator and Supplier methods were called the correct number of times
        verify(idGenerator, times(3)).generateBillId(anyInt(), anyInt());
        verify(billSupplier, times(3)).findOrCreateBill(anyInt(), anyInt());
    }

    @Test
    public void testProcessEmptyBillList() {
        String json = "{ \"bills\": [], \"pagination\": { \"count\": 0 }, \"request\": { \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(0, finalBillList.size());
    }

    @Test
    public void testProcessBillWithMissingFields() {
        String json = "{ \"bills\": [ { \"congress\": 118, \"number\": \"1234\" } ], \"pagination\": { \"count\": 1 }, \"request\": { \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

        when(billSupplier.findOrCreateBill(anyInt(), anyInt())).thenAnswer(invocation -> new Bill());

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

        when(billSupplier.findOrCreateBill(anyInt(), anyInt())).thenAnswer(invocation -> new Bill());

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(2, finalBillList.size());

        // First Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 4367 &&
                bill.getBillTitle().equals("Thomas R. Carper Water Resources Development Act of 2024") &&
                bill.getLatestActionTxt().equals("Message on Senate action sent to the House.") &&
                bill.getLatestActionDt().equals(LocalDate.of(2024, 8, 2))));

        // Second Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 1234 &&
                bill.getBillTitle().equals("A Sample Bill for Testing") &&
                bill.getLatestActionTxt().equals("Referred to Committee.") &&
                bill.getLatestActionDt().equals(LocalDate.of(2024, 7, 15))));
    }

    @Test
    public void testProcessNullBillList() {
        String json = "{ \"bills\": null, \"pagination\": { \"count\": 0 }, \"request\": { \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

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
                "            \"congress\": 118,\n" +
                "            \"number\": \"0000\"\n" + // Invalid data (no title, no type, etc.)
                "        }\n" +
                "    ]\n" +
                "}";

        when(billSupplier.findOrCreateBill(anyInt(), anyInt())).thenAnswer(invocation -> new Bill());

        List<Bill> finalBillList = billByCongressProcessor.processBillList(json);

        assertEquals(2, finalBillList.size());

        // Valid Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 4367 &&
                bill.getBillTitle().equals("Thomas R. Carper Water Resources Development Act of 2024")));

        // Invalid Bill
        assertTrue(finalBillList.stream().anyMatch(bill -> bill.getBillNo() == 0)); // Bill with missing data
    }



}
