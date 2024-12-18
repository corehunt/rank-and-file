package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.RelatedBillController;
import com.rankandfile.backend.service.external.bill.RelatedBillService;
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
@WebMvcTest(RelatedBillController.class)
class RelatedBillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RelatedBillService relatedBillService;

    /**
     * Test the successful loading of related bills.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testLoadRelatedBillsSuccess() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        // No exception is thrown by the service, implying success
        Mockito.doNothing().when(relatedBillService).getRelatedBills(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/relatedBills", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded related bills for bill number: " + billNumber));

        Mockito.verify(relatedBillService, times(1)).getRelatedBills(congressNo, billType, billNumber);
    }

    /**
     * Test loading related bills when the bill is not found.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testLoadRelatedBillsBillNotFound() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999"; // Assuming this bill number does not exist

        // Service throws EntityNotFoundException
        Mockito.doThrow(new EntityNotFoundException("Bill not found"))
                .when(relatedBillService).getRelatedBills(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/relatedBills", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        Mockito.verify(relatedBillService, times(1)).getRelatedBills(congressNo, billType, billNumber);
    }

    /**
     * Test loading related bills when an internal server error occurs.
     *
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testLoadRelatedBillsInternalServerError() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        // Service throws a generic exception
        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(relatedBillService).getRelatedBills(congressNo, billType, billNumber);

        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/relatedBills", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load related bills for bill number: " + billNumber));

        Mockito.verify(relatedBillService, times(1)).getRelatedBills(congressNo, billType, billNumber);
    }
}
