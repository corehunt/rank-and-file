package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.BillSummaryController;
import com.rankandfile.backend.service.external.bill.BillSummaryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BillSummaryController.class)
class BillSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillSummaryService billSummaryService;

    @Test
    void testLoadBillSummarySuccess() throws Exception {
        int congressNo = 117;
        String billType = "hr";
        int billNumber = 3076;

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/summary", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded summary for bill number: " + billNumber));

        verify(billSummaryService, times(1)).fetchBillSummary(congressNo, billType, billNumber);
    }

    @Test
    void testLoadBillSummaryBillNotFound() throws Exception {
        int congressNo = 117;
        String billType = "hr";
        int billNumber = 9999;

        doThrow(new EntityNotFoundException("Bill not found"))
                .when(billSummaryService).fetchBillSummary(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/summary", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        verify(billSummaryService, times(1)).fetchBillSummary(congressNo, billType, billNumber);
    }

    @Test
    void testLoadBillSummaryInternalServerError() throws Exception {
        int congressNo = 117;
        String billType = "hr";
        int billNumber = 3076;

        doThrow(new RuntimeException("Unexpected error"))
                .when(billSummaryService).fetchBillSummary(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/summary", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load summary for bill number: " + billNumber));

        verify(billSummaryService, times(1)).fetchBillSummary(congressNo, billType, billNumber);
    }
}
