package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.MemberCongressController;
import com.rankandfile.backend.service.external.person.CongressClassPersonService;
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
@WebMvcTest(MemberCongressController.class)
class MemberCongressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CongressClassPersonService congressClassPersonService;

    private static final int LIMIT = 250;

    @Test
    void testLoadMembersOfCongressSuccess() throws Exception {
        String congressId = "117";

        mockMvc.perform(put("/api/member/congress/{congressId}", congressId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded and saved members of Congress: " + congressId));

        Mockito.verify(congressClassPersonService, times(1))
                .fetchMembersOfCurrentCongress(congressId, LIMIT);
    }

    @Test
    void testLoadMembersOfCongressCongressNotFound() throws Exception {
        String congressId = "999";

        Mockito.doThrow(new EntityNotFoundException("Congress not found"))
                .when(congressClassPersonService).fetchMembersOfCurrentCongress(congressId, LIMIT);

        mockMvc.perform(put("/api/member/congress/{congressId}", congressId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Congress not found: Congress not found"));

        Mockito.verify(congressClassPersonService, times(1))
                .fetchMembersOfCurrentCongress(congressId, LIMIT);
    }

    @Test
    void testLoadMembersOfCongressInternalServerError() throws Exception {
        String congressId = "117";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(congressClassPersonService).fetchMembersOfCurrentCongress(congressId, LIMIT);

        mockMvc.perform(put("/api/member/congress/{congressId}", congressId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load members of Congress: " + congressId));

        Mockito.verify(congressClassPersonService, times(1))
                .fetchMembersOfCurrentCongress(congressId, LIMIT);
    }
}
