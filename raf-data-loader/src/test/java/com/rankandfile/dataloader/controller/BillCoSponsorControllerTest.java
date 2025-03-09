package com.rankandfile.dataloader.controller;

import com.rankandfile.dataloader.controller.external.BillCoSponsorController;
import com.rankandfile.dataloader.service.external.bill.BillCoSponsorService;
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
@WebMvcTest(BillCoSponsorController.class)
class BillCoSponsorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillCoSponsorService billCoSponsorService;

    @Test
    void testLoadCoSponsorsForBillNumberSuccess() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "1234";

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/cosponsors", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded committees for bill number: " + billNumber));

        Mockito.verify(billCoSponsorService, times(1)).getCoSponsorsByBillNumber(congressNo, billType, billNumber);
    }

    @Test
    void testLoadCoSponsorsForBillNumberBillNotFound() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999";

        Mockito.doThrow(new EntityNotFoundException("Bill not found"))
                .when(billCoSponsorService).getCoSponsorsByBillNumber(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/cosponsors", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        Mockito.verify(billCoSponsorService, times(1)).getCoSponsorsByBillNumber(congressNo, billType, billNumber);
    }

    @Test
    void testLoadCoSponsorsForBillNumberInternalServerError() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "1234";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(billCoSponsorService).getCoSponsorsByBillNumber(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/cosponsors", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load co-sponsors for bill number: " + billNumber));

        Mockito.verify(billCoSponsorService, times(1)).getCoSponsorsByBillNumber(congressNo, billType, billNumber);
    }
}
