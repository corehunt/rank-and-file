package com.rankandfile.backend.controller;

import com.rankandfile.backend.controller.external.MemberController;
import com.rankandfile.backend.service.external.person.MemberService;
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
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    void testLoadMemberSuccess() throws Exception {
        String bioguideId = "A000360";

        mockMvc.perform(put("/api/member/{bioguideId}", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded and saved member with bioguideId: " + bioguideId));

        Mockito.verify(memberService, times(1)).fetchAndProcessPerson(bioguideId);
    }

    @Test
    void testLoadMemberMemberNotFound() throws Exception {
        String bioguideId = "INVALID_ID";

        Mockito.doThrow(new EntityNotFoundException("Member not found"))
                .when(memberService).fetchAndProcessPerson(bioguideId);

        mockMvc.perform(put("/api/member/{bioguideId}", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Member not found: Member not found"));

        Mockito.verify(memberService, times(1)).fetchAndProcessPerson(bioguideId);
    }

    @Test
    void testLoadMemberInternalServerError() throws Exception {
        String bioguideId = "A000360";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(memberService).fetchAndProcessPerson(bioguideId);

        mockMvc.perform(put("/api/member/{bioguideId}", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load member with bioguideId: " + bioguideId));

        Mockito.verify(memberService, times(1)).fetchAndProcessPerson(bioguideId);
    }
}
