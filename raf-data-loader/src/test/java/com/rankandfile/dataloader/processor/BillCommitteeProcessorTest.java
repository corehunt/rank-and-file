package com.rankandfile.dataloader.processor;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Committee;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.repository.CommitteeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillCommitteeProcessorTest {

    @Mock
    private CommitteeRepository committeeRepository;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillCommitteeProcessor billCommitteeProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessSuccessfulProcessing() {
        String billId = "BILL-1234";
        String json = "{\n" +
                "    \"committees\": [\n" +
                "        {\n" +
                "            \"systemCode\": \"COM1\",\n" +
                "            \"subcommittees\": [\n" +
                "                {\n" +
                "                    \"systemCode\": \"SUBCOM1\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"systemCode\": \"COM2\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Mock CommitteeRepository to return Committees
        Committee committee1 = new Committee();
        committee1.setCommitteeId("COMMITTEE-1");
        committee1.setSysCode("COM1");

        Committee subcommittee1 = new Committee();
        subcommittee1.setCommitteeId("COMMITTEE-1-1");
        subcommittee1.setSysCode("SUBCOM1");

        Committee committee2 = new Committee();
        committee2.setCommitteeId("COMMITTEE-2");
        committee2.setSysCode("COM2");

        when(committeeRepository.findBySysCode("COM1")).thenReturn(committee1);
        when(committeeRepository.findBySysCode("SUBCOM1")).thenReturn(subcommittee1);
        when(committeeRepository.findBySysCode("COM2")).thenReturn(committee2);

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verify(committeeRepository).findBySysCode("COM1");
        verify(committeeRepository).findBySysCode("SUBCOM1");
        verify(committeeRepository).findBySysCode("COM2");

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());

        List<Committee> committees = result.getCommittees();
        assertNotNull(committees);
        assertEquals(3, committees.size());
        assertTrue(committees.contains(committee1));
        assertTrue(committees.contains(subcommittee1));
        assertTrue(committees.contains(committee2));
    }

    @Test
    void testProcessBillNotFound() {
        String billId = "BILL-1234";
        String json = "{}";

        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verifyNoInteractions(committeeRepository);

        // Assertions
        assertNull(result);
    }

    @Test
    void testProcessNoCommitteesInJson() {
        String billId = "BILL-1234";
        String json = "{}";

        // Mock BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verifyNoInteractions(committeeRepository);

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());
        assertTrue(result.getCommittees() == null || result.getCommittees().isEmpty());
    }

    @Test
    void testProcessInvalidJson() {
        String billId = "BILL-1234";
        String json = "{ invalid json ";

        // Mock BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verifyNoInteractions(committeeRepository);

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());
        assertTrue(result.getCommittees() == null || result.getCommittees().isEmpty());
    }

    @Test
    void testProcessCommitteeSystemCodeMissing() {
        String billId = "BILL-1234";
        String json = "{\n" +
                "    \"committees\": [\n" +
                "        {\n" +
                "            \"name\": \"Committee Without SystemCode\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verifyNoInteractions(committeeRepository);

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());
        assertTrue(result.getCommittees() == null || result.getCommittees().isEmpty());
    }

    @Test
    void testProcessCommitteeNotFoundInRepository() {
        String billId = "BILL-1234";
        String json = "{\n" +
                "    \"committees\": [\n" +
                "        {\n" +
                "            \"systemCode\": \"UNKNOWN\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Mock CommitteeRepository to return null
        when(committeeRepository.findBySysCode("UNKNOWN")).thenReturn(null);

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verify(committeeRepository).findBySysCode("UNKNOWN");

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());
        assertTrue(result.getCommittees() == null || result.getCommittees().isEmpty());
    }

    @Test
    void testProcessSubcommitteeSystemCodeMissing() {
        String billId = "BILL-1234";
        String json = "{\n" +
                "    \"committees\": [\n" +
                "        {\n" +
                "            \"systemCode\": \"COM1\",\n" +
                "            \"subcommittees\": [\n" +
                "                {\n" +
                "                    \"name\": \"Subcommittee Without SystemCode\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Mock BillRepository to return a Bill
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Mock CommitteeRepository
        Committee committee1 = new Committee();
        committee1.setCommitteeId("COMMITTEE-1");
        committee1.setSysCode("COM1");

        when(committeeRepository.findBySysCode("COM1")).thenReturn(committee1);

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verify(committeeRepository).findBySysCode("COM1");
        verifyNoMoreInteractions(committeeRepository);

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());
        List<Committee> committees = result.getCommittees();
        assertNotNull(committees);
        assertEquals(1, committees.size());
        assertTrue(committees.contains(committee1));
    }

    @Test
    void testProcessDuplicateCommittees() {
        String billId = "BILL-1234";
        String json = "{\n" +
                "    \"committees\": [\n" +
                "        { \"systemCode\": \"COM1\" },\n" +
                "        { \"systemCode\": \"COM1\" }\n" +
                "    ]\n" +
                "}";

        // Mock BillRepository
        Bill bill = new Bill();
        bill.setBillId(billId);
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        // Mock CommitteeRepository
        Committee committee1 = new Committee();
        committee1.setCommitteeId("COMMITTEE-1");
        committee1.setSysCode("COM1");

        when(committeeRepository.findBySysCode("COM1")).thenReturn(committee1);

        // Execute the method
        Bill result = billCommitteeProcessor.process(json, billId);

        // Verify interactions
        verify(billRepository).findById(billId);
        verify(committeeRepository, times(2)).findBySysCode("COM1");

        // Assertions
        assertNotNull(result);
        assertEquals(billId, result.getBillId());
        List<Committee> committees = result.getCommittees();
        assertNotNull(committees);
        assertEquals(1, committees.size());
        assertTrue(committees.contains(committee1));
    }
}
