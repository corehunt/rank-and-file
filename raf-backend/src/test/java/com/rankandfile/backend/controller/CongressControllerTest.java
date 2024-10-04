package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.CongressController;
import com.rankandfile.backend.service.external.congress.CongressService;
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
@WebMvcTest(CongressController.class)
class CongressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CongressService congressService;

    @Test
    void testFetchAndSaveCongressSuccess() throws Exception {
        String congressNo = "117";

        mockMvc.perform(put("/api/congress/{congressNo}", congressNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded Congress data for Congress number: " + congressNo));

        Mockito.verify(congressService, times(1)).fetchAndSaveCongressByNumber(congressNo);
    }

    @Test
    void testFetchAndSaveCongressCongressNotFound() throws Exception {
        String congressNo = "999";

        Mockito.doThrow(new EntityNotFoundException("Congress not found"))
                .when(congressService).fetchAndSaveCongressByNumber(congressNo);

        mockMvc.perform(put("/api/congress/{congressNo}", congressNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Congress not found: Congress not found"));

        Mockito.verify(congressService, times(1)).fetchAndSaveCongressByNumber(congressNo);
    }

    @Test
    void testFetchAndSaveCongressInternalServerError() throws Exception {
        String congressNo = "117";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(congressService).fetchAndSaveCongressByNumber(congressNo);

        mockMvc.perform(put("/api/congress/{congressNo}", congressNo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load Congress data for Congress number: " + congressNo));

        Mockito.verify(congressService, times(1)).fetchAndSaveCongressByNumber(congressNo);
    }
}
