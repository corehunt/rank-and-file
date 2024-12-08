package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.bill.BillActionService;
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
@WebMvcTest(BillActionController.class)
class BillActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillActionService billActionService;

    private static final int LIMIT = 250;

    @Test
    void testLoadActionsForBillNumber_Success() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "1234";

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/actions", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded actions for bill number: " + billNumber));

        Mockito.verify(billActionService, times(1)).getActionsByBillNumber(congressNo, billType, billNumber, LIMIT);
    }

    @Test
    void testLoadActionsForBillNumber_BillNotFound() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999";

        Mockito.doThrow(new EntityNotFoundException("Bill not found"))
                .when(billActionService).getActionsByBillNumber(congressNo, billType, billNumber, LIMIT);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/actions", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        Mockito.verify(billActionService, times(1)).getActionsByBillNumber(congressNo, billType, billNumber, LIMIT);
    }

    @Test
    void testLoadActionsForBillNumber_InternalServerError() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "1234";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(billActionService).getActionsByBillNumber(congressNo, billType, billNumber, LIMIT);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/actions", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load actions for bill number: " + billNumber));

        Mockito.verify(billActionService, times(1)).getActionsByBillNumber(congressNo, billType, billNumber, LIMIT);
    }
}
