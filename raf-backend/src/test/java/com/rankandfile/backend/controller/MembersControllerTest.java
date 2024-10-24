package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.MembersController;
import com.rankandfile.backend.service.external.person.MembersService;
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
@WebMvcTest(MembersController.class)
class MembersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MembersService membersService;

    private static final int LIMIT = 250;

    @Test
    void testLoadAllMembersSuccess() throws Exception {
        mockMvc.perform(put("/api/member/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded and saved members"));

        Mockito.verify(membersService, times(1)).fetchAndSaveMembers(LIMIT);
    }

    @Test
    void testLoadAllMembersInternalServerError() throws Exception {
        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(membersService).fetchAndSaveMembers(LIMIT);

        mockMvc.perform(put("/api/member/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load all members: Unexpected error"));

        Mockito.verify(membersService, times(1)).fetchAndSaveMembers(LIMIT);
    }
}
