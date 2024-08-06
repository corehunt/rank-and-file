package com.rankandfile.backend.processor;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BillByCongressTypeNumberProcessorTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessBillWithAllFields() {
        String json = "{ \"bill\": { \"actions\": { \"count\": 3, \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/actions?format=json\" }, \"committees\": { \"count\": 1, \"url\": \"https://api.congress.gov/v3/bill/118/hr/6937/committees?format=json\" }, \"congress\": 118, \"introducedDate\": \"2024-01-10\", \"latestAction\": { \"actionDate\": \"2024-01-10\", \"text\": \"Referred to the House Committee on Agriculture.\" }, \"number\": \"6937\", \"originChamber\": \"House\", \"originChamberCode\": \"H\", \"policyArea\": { \"name\": \"Agriculture and Food\" }, \"title\": \"The Organic Dairy Data Collection Act\", \"type\": \"HR\", \"updateDate\": \"2024-06-13T15:09:04Z\", \"updateDateIncludingText\": \"2024-06-13T15:09:04Z\" }, \"request\": { \"billNumber\": \"6937\", \"billType\": \"hr\", \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

        Bill mockBill = new Bill();
        when(billRepository.findByCongressAndBillNo(anyInt(), anyInt())).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertEquals(118, processedBill.getCongress());
        assertEquals(6937, processedBill.getBillNo());
        assertEquals("The Organic Dairy Data Collection Act", processedBill.getBillTitle());
        assertEquals(LocalDate.of(2024, 1, 10), processedBill.getIntroducedDt());
        assertEquals(LocalDate.of(2024, 1, 10), processedBill.getLatestActionDt());
        assertEquals("Referred to the House Committee on Agriculture.", processedBill.getLatestActionTxt());
        assertEquals("House", processedBill.getOriginChamber());
        assertEquals("H", processedBill.getOriginChamberCd());
        assertEquals("Agriculture and Food", processedBill.getPolicyArea());
    }

    @Test
    public void testProcessBillWithMissingFields() {
        String json = "{ \"bill\": { \"congress\": 118, \"number\": \"6937\" }, \"request\": { \"billNumber\": \"6937\", \"billType\": \"hr\", \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

        Bill mockBill = new Bill();
        when(billRepository.findByCongressAndBillNo(anyInt(), anyInt())).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertEquals(118, processedBill.getCongress());
        assertEquals(6937, processedBill.getBillNo());
        assertNull(processedBill.getBillTitle());
        assertNull(processedBill.getIntroducedDt());
        assertNull(processedBill.getLatestActionDt());
        assertNull(processedBill.getLatestActionTxt());
        assertNull(processedBill.getOriginChamber());
        assertNull(processedBill.getOriginChamberCd());
        assertNull(processedBill.getPolicyArea());
    }

    @Test
    public void testProcessBillWithNullFields() {
        String json = "{ \"bill\": { \"congress\": 118, \"number\": null }, \"request\": { \"billNumber\": \"6937\", \"billType\": \"hr\", \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

        Bill mockBill = new Bill();
        when(billRepository.findByCongressAndBillNo(anyInt(), anyInt())).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertEquals(118, processedBill.getCongress());
        assertNull(processedBill.getBillNo());
        assertNull(processedBill.getBillTitle());
        assertNull(processedBill.getIntroducedDt());
        assertNull(processedBill.getLatestActionDt());
        assertNull(processedBill.getLatestActionTxt());
        assertNull(processedBill.getOriginChamber());
        assertNull(processedBill.getOriginChamberCd());
        assertNull(processedBill.getPolicyArea());
    }

    @Test
    public void testProcessBillWithEmptyFields() {
        String json = "{ \"bill\": { \"congress\": 118, \"number\": \"6937\", \"title\": \"\", \"introducedDate\": \"\", \"latestAction\": { \"actionDate\": \"\", \"text\": \"\" }, \"originChamber\": \"\", \"originChamberCode\": \"\", \"policyArea\": { \"name\": \"\" } }, \"request\": { \"billNumber\": \"6937\", \"billType\": \"hr\", \"congress\": \"118\", \"contentType\": \"application/json\", \"format\": \"json\" } }";

        Bill mockBill = new Bill();
        when(billRepository.findByCongressAndBillNo(anyInt(), anyInt())).thenReturn(mockBill);

        Bill processedBill = billByCongressTypeNumberProcessor.process(json);

        assertEquals(118, processedBill.getCongress());
        assertEquals(6937, processedBill.getBillNo());
        assertEquals("", processedBill.getBillTitle());
        assertNull(processedBill.getIntroducedDt());
        assertNull(processedBill.getLatestActionDt());
        assertEquals("", processedBill.getLatestActionTxt());
        assertEquals("", processedBill.getOriginChamber());
        assertEquals("", processedBill.getOriginChamberCd());
        assertEquals("", processedBill.getPolicyArea());
    }
}
