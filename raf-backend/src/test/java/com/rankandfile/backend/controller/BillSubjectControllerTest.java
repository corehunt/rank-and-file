package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.BillSubjectController;
import com.rankandfile.backend.service.external.bill.BillSubjectService;
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

/**
 * Test class for BillSubjectController.
 *
 * This class tests the BillSubjectController's endpoints to ensure they behave as expected
 * under various scenarios, including successful processing, handling of not found entities,
 * and internal server errors.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(BillSubjectController.class)
class BillSubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillSubjectService billSubjectService;

    /**
     * Test the successful loading of bill subjects.
     *
     * This test ensures that when a valid request is made, the controller invokes the service
     * correctly and returns an HTTP 200 OK status with the expected success message.
     */
    @Test
    void testLoadBillSubjectsSuccess() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        // Perform PUT request to the specified endpoint
        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/subjects", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded bill subjects for bill number: " + billNumber));

        // Verify that the service method was called once with the correct parameters
        Mockito.verify(billSubjectService, times(1)).fetchBillSubjects(congressNo, billType, billNumber);
    }

    /**
     * Test loading bill subjects when the bill is not found.
     *
     * This test ensures that if the service throws an EntityNotFoundException, the controller
     * responds with an HTTP 404 Not Found status and the appropriate error message.
     */
    @Test
    void testLoadBillSubjectsBillNotFound() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "9999"; // Assuming this bill number does not exist

        // Configure the service to throw EntityNotFoundException when invoked with the specified parameters
        Mockito.doThrow(new EntityNotFoundException("Bill not found"))
                .when(billSubjectService).fetchBillSubjects(congressNo, billType, billNumber);

        // Perform PUT request to the specified endpoint
        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/subjects", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Bill not found: Bill not found"));

        // Verify that the service method was called once with the correct parameters
        Mockito.verify(billSubjectService, times(1)).fetchBillSubjects(congressNo, billType, billNumber);
    }

    /**
     * Test loading bill subjects when an internal server error occurs.
     *
     * This test ensures that if the service throws a generic exception, the controller
     * responds with an HTTP 500 Internal Server Error status and the appropriate error message.
     */
    @Test
    void testLoadBillSubjectsInternalServerError() throws Exception {
        String congressNo = "117";
        String billType = "hr";
        String billNumber = "3076";

        // Configure the service to throw a generic RuntimeException when invoked with the specified parameters
        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(billSubjectService).fetchBillSubjects(congressNo, billType, billNumber);

        // Perform PUT request to the specified endpoint
        mockMvc.perform(put("/api/bill/{congressNo}/{billType}/{billNumber}/subjects", congressNo, billType, billNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load bill texts for bill number: " + billNumber));

        // Verify that the service method was called once with the correct parameters
        Mockito.verify(billSubjectService, times(1)).fetchBillSubjects(congressNo, billType, billNumber);
    }
}
