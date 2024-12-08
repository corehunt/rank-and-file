package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.BillController;
import com.rankandfile.backend.service.external.bill.BillByCongressService;
import com.rankandfile.backend.service.external.bill.BillByTypeAndNumberService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillByCongressService billByCongressService;

    @MockBean
    private BillByTypeAndNumberService billByTypeAndNumberService;

    private static final int LIMIT = 250;

    @Test
    void testLoadBillsByCongressSuccess() throws Exception {
        String congressNo = "117";

        mockMvc.perform(put("/api/bill/{congressNo}", congressNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded bills for Congress number: " + congressNo));

        Mockito.verify(billByCongressService, times(1)).getBillsByCongress(congressNo, LIMIT);
    }

    @Test
    void testLoadBillsByCongressCongressNotFound() throws Exception {
        String congressNo = "999";

        Mockito.doThrow(new EntityNotFoundException("Congress not found"))
                .when(billByCongressService).getBillsByCongress(congressNo, LIMIT);

        mockMvc.perform(put("/api/bill/{congressNo}", congressNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Congress not found: Congress not found"));

        Mockito.verify(billByCongressService, times(1)).getBillsByCongress(congressNo, LIMIT);
    }

    @Test
    void testLoadBillsByCongressInternalServerError() throws Exception {
        String congressNo = "117";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(billByCongressService).getBillsByCongress(congressNo, LIMIT);

        mockMvc.perform(put("/api/bill/{congressNo}", congressNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load bills for Congress number: " + congressNo));

        Mockito.verify(billByCongressService, times(1)).getBillsByCongress(congressNo, LIMIT);
    }

    @Test
    void testLoadBillDataByTypeAndNumberSuccess() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "1234";

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded data for bill number: " + billNumber));

        Mockito.verify(billByTypeAndNumberService, times(1))
                .getBillByTypeAndNumber(congressNo, billType, billNumber);
    }

    @Test
    void testLoadBillDataByTypeAndNumberBillNotFound() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999";

        Mockito.doThrow(new EntityNotFoundException("Bill not found"))
                .when(billByTypeAndNumberService).getBillByTypeAndNumber(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        Mockito.verify(billByTypeAndNumberService, times(1))
                .getBillByTypeAndNumber(congressNo, billType, billNumber);
    }

    @Test
    void testLoadBillDataByTypeAndNumberInternalServerError() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "1234";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(billByTypeAndNumberService).getBillByTypeAndNumber(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load data for bill number: " + billNumber));

        Mockito.verify(billByTypeAndNumberService, times(1))
                .getBillByTypeAndNumber(congressNo, billType, billNumber);
    }
}
