package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.CommitteeController;
import com.rankandfile.backend.service.external.committee.CommitteeService;
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
@WebMvcTest(CommitteeController.class)
class CommitteeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommitteeService committeeService;

    private static final int LIMIT = 250;

    @Test
    void testLoadAllCommitteesSuccess() throws Exception {
        mockMvc.perform(put("/api/committee/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded committees"));

        Mockito.verify(committeeService, times(1)).fetchAndProcessCommittees(LIMIT);
    }

    @Test
    void testLoadAllCommitteesServiceThrowsException() throws Exception {
        Mockito.doThrow(new RuntimeException("Service error"))
                .when(committeeService).fetchAndProcessCommittees(LIMIT);

        mockMvc.perform(put("/api/committee/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load committees"));

        Mockito.verify(committeeService, times(1)).fetchAndProcessCommittees(LIMIT);
    }
}
