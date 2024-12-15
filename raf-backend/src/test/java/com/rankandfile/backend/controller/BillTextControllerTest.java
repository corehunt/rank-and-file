package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.BillTextController;
import com.rankandfile.backend.service.external.bill.BillTextService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BillTextController.class)
class BillTextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillTextService billTextService;

    @Test
    void testLoadBillTextsSuccess() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/text", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded bill texts for bill number: " + billNumber));

        Mockito.verify(billTextService, times(1)).fetchBillTexts(congressNo, billType, billNumber);
    }

    @Test
    void testLoadBillTextsBillNotFound() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999";

        Mockito.doThrow(new EntityNotFoundException("Bill not found"))
                .when(billTextService).fetchBillTexts(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/text", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        Mockito.verify(billTextService, times(1)).fetchBillTexts(congressNo, billType, billNumber);
    }

    @Test
    void testLoadBillTextsInternalServerError() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(billTextService).fetchBillTexts(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/text", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load bill texts for bill number: " + billNumber));

        Mockito.verify(billTextService, times(1)).fetchBillTexts(congressNo, billType, billNumber);
    }
}
