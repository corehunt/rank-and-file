package com.rankandfile.dataloader.processor;

import com.google.gson.JsonSyntaxException;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentBillProcessorTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private RecentBillProcessor recentBillProcessor;

    @Test
    void testProcessRecentBillswithMultipleValidBills() {
        // Given: Two valid bill records in JSON.
        String json = "{\n" +
                "  \"bills\": [\n" +
                "    {\n" +
                "      \"congress\": \"119\",\n" +
                "      \"latestAction\": { \"actionDate\": \"2025-01-27\", \"text\": \"Action text for bill 40.\" },\n" +
                "      \"number\": \"40\",\n" +
                "      \"originChamber\": \"Senate\",\n" +
                "      \"originChamberCode\": \"S\",\n" +
                "      \"title\": \"A resolution commemorating the anniversary.\",\n" +
                "      \"type\": \"SRES\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"congress\": \"119\",\n" +
                "      \"latestAction\": { \"actionDate\": \"2025-01-23\", \"text\": \"Action text for bill 33.\" },\n" +
                "      \"number\": \"33\",\n" +
                "      \"originChamber\": \"Senate\",\n" +
                "      \"originChamberCode\": \"S\",\n" +
                "      \"title\": \"A resolution expressing support.\",\n" +
                "      \"type\": \"SRES\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Stub the IdGenerator. For example, generate a composite ID as "congress-type-number".
        when(idGenerator.generateBillId(anyString(), anyString(), anyString()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0) + "-" + invocation.getArgument(1) + "-" + invocation.getArgument(2));

        // Stub the repository: return null for both so that new bills are created.
        when(billRepository.findByCongressAndBillNoAndBillType(anyString(), anyString(), anyString()))
                .thenReturn(null);

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then: two bills should be processed.
        assertEquals(2, bills.size());

        // Verify first bill.
        Bill bill1 = bills.stream().filter(b -> Objects.equals(b.getBillNo(), "40")).findFirst().orElse(null);
        assertNotNull(bill1);
        assertEquals("119", bill1.getCongress());
        assertEquals("40", bill1.getBillNo());
        assertEquals("A resolution commemorating the anniversary.", bill1.getBillTitle());
        assertEquals("SRES", bill1.getBillType());
        assertEquals("Senate", bill1.getOriginChamber());
        assertEquals("S", bill1.getOriginChamberCd());
        assertEquals(LocalDate.of(2025, 1, 27), bill1.getLatestActionDt());
        assertEquals("Action text for bill 40.", bill1.getLatestActionTxt());

        // Verify second bill.
        Bill bill2 = bills.stream().filter(b -> Objects.equals(b.getBillNo(), "33")).findFirst().orElse(null);
        assertNotNull(bill2);
        assertEquals("119", bill2.getCongress());
        assertEquals("33", bill2.getBillNo());
        assertEquals("A resolution expressing support.", bill2.getBillTitle());
        assertEquals("SRES", bill2.getBillType());
        assertEquals("Senate", bill2.getOriginChamber());
        assertEquals("S", bill2.getOriginChamberCd());
        assertEquals(LocalDate.of(2025, 1, 23), bill2.getLatestActionDt());
        assertEquals("Action text for bill 33.", bill2.getLatestActionTxt());

        // Verify that IdGenerator.generateBillId() was called for each bill.
        verify(idGenerator, times(2)).generateBillId(anyString(), anyString(), anyString());
    }

    @Test
    void testProcessRecentBillswithEmptyBillsArray() {
        // Given an empty bills array.
        String json = "{ \"bills\": [] }";

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then
        assertTrue(bills.isEmpty());
    }

    @Test
    void testProcessRecentBillswithNullBillsField() {
        // Given bills field is explicitly null.
        String json = "{ \"bills\": null }";

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then
        assertTrue(bills.isEmpty());
    }

    @Test
    void testProcessRecentBillswithMissingRequiredFields() {
        // Given a bill record missing congress, number, or type.
        String json = "{ \"bills\": [ { \"title\": \"Missing required fields\" } ] }";

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then: the bill should be skipped.
        assertEquals(0, bills.size());
    }

    @Test
    void testProcessRecentBillswithInvalidLatestActionDate() {
        // Given a bill record with an invalid date in latestAction.
        String json = "{ \"bills\": [ { " +
                "\"congress\": \"118\", " +
                "\"latestAction\": { \"actionDate\": \"invalid-date\", \"text\": \"Action with invalid date\" }, " +
                "\"number\": \"1234\", " +
                "\"originChamber\": \"House\", " +
                "\"originChamberCode\": \"H\", " +
                "\"title\": \"Bill with invalid date\", " +
                "\"type\": \"HR\" " +
                "} ] }";

        // Stub the repository so that no existing bill is returned.
        when(billRepository.findByCongressAndBillNoAndBillType(anyString(), anyString(), anyString()))
                .thenReturn(null);

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then
        assertEquals(1, bills.size());
        Bill bill = bills.get(0);
        // latestActionDt should be null due to date parsing error.
        assertNull(bill.getLatestActionDt());
        assertEquals("Action with invalid date", bill.getLatestActionTxt());
    }

    @Test
    void testProcessRecentBillswithExistingBillAndUpdates() {
        // Given a JSON with one bill.
        String json = "{ \"bills\": [ { " +
                "\"congress\": \"118\", " +
                "\"latestAction\": { \"actionDate\": \"2024-08-02\", \"text\": \"Updated action text.\" }, " +
                "\"number\": \"4367\", " +
                "\"originChamber\": \"Senate\", " +
                "\"originChamberCode\": \"S\", " +
                "\"title\": \"Updated Bill Title\", " +
                "\"type\": \"S\" " +
                "} ] }";

        // Prepare an existing bill that should be updated.
        Bill existingBill = new Bill();
        existingBill.setBillId("118-S4367");
        existingBill.setCongress("118");
        existingBill.setBillNo("4367");
        existingBill.setBillTitle("Original Title");
        existingBill.setLatestActionDt(LocalDate.of(2024, 8, 1));
        existingBill.setLatestActionTxt("Original action text.");
        existingBill.setBillType("S");

        // Stub the repository to return the existing bill.
        when(billRepository.findByCongressAndBillNoAndBillType("118", "4367", "S"))
                .thenReturn(existingBill);

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then: the existing bill should be updated.
        assertEquals(1, bills.size());
        Bill bill = bills.get(0);
        assertEquals("Updated Bill Title", bill.getBillTitle());
        assertEquals(LocalDate.of(2024, 8, 2), bill.getLatestActionDt());
        assertEquals("Updated action text.", bill.getLatestActionTxt());
    }

    @Test
    void testProcessRecentBillswithMalformedJson() {
        // Given a malformed JSON string.
        String json = "{ \"bills\": [ { \"congress\": \"118\", \"number\": \"1234\", ";

        // Then: processing should throw a JsonSyntaxException.
        assertThrows(JsonSyntaxException.class, () -> recentBillProcessor.processRecentBills(json));
    }

    @Test
    void testProcessRecentBillswithEmptyJsonObject() {
        // Given an empty JSON object.
        String json = "{}";

        // When
        List<Bill> bills = recentBillProcessor.processRecentBills(json);

        // Then
        assertTrue(bills.isEmpty());
    }

    @Test
    void testProcessRecentBillswithNullJson() {
        // Given a null JSON string.
        String json = null;

        // Then: a NullPointerException is expected.
        assertThrows(NullPointerException.class, () -> recentBillProcessor.processRecentBills(json));
    }
}
