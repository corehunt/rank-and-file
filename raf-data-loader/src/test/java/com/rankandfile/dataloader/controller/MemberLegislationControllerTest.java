package com.rankandfile.dataloader.controller;

import com.rankandfile.dataloader.controller.external.MemberLegislationController;
import com.rankandfile.dataloader.service.external.person.MemberCoSponsLegislationService;
import com.rankandfile.dataloader.service.external.person.MemberSponsLegislationService;
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
@WebMvcTest(MemberLegislationController.class)
class MemberLegislationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberSponsLegislationService memberSponsLegislationService;

    @MockBean
    private MemberCoSponsLegislationService memberCoSponsLegislationService;

    private static final int LIMIT = 250;

    @Test
    void testLoadSponsoredLegislationByMemberSuccess() throws Exception {
        String bioguideId = "A000360";

        mockMvc.perform(put("/api/member/{bioguideId}/sponsored-legislation", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded sponsored legislation for bioguideId: " + bioguideId));

        Mockito.verify(memberSponsLegislationService, times(1))
                .getSponsoredLegislationByPersonId(bioguideId, LIMIT);
    }

    @Test
    void testLoadSponsoredLegislationByMemberMemberNotFound() throws Exception {
        String bioguideId = "INVALID_ID";

        Mockito.doThrow(new EntityNotFoundException("Member not found"))
                .when(memberSponsLegislationService).getSponsoredLegislationByPersonId(bioguideId, LIMIT);

        mockMvc.perform(put("/api/member/{bioguideId}/sponsored-legislation", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Member not found: Member not found"));

        Mockito.verify(memberSponsLegislationService, times(1))
                .getSponsoredLegislationByPersonId(bioguideId, LIMIT);
    }

    @Test
    void testLoadSponsoredLegislationByMemberInternalServerError() throws Exception {
        String bioguideId = "A000360";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(memberSponsLegislationService).getSponsoredLegislationByPersonId(bioguideId, LIMIT);

        mockMvc.perform(put("/api/member/{bioguideId}/sponsored-legislation", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load sponsored legislation for bioguideId: " + bioguideId));

        Mockito.verify(memberSponsLegislationService, times(1))
                .getSponsoredLegislationByPersonId(bioguideId, LIMIT);
    }

    @Test
    void testLoadCoSponsoredLegislationByMemberSuccess() throws Exception {
        String bioguideId = "A000360";

        mockMvc.perform(put("/api/member/{bioguideId}/cosponsored-legislation", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully loaded co-sponsored legislation for bioguideId: " + bioguideId));

        Mockito.verify(memberCoSponsLegislationService, times(1))
                .getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);
    }

    @Test
    void testLoadCoSponsoredLegislationByMemberMemberNotFound() throws Exception {
        String bioguideId = "INVALID_ID";

        Mockito.doThrow(new EntityNotFoundException("Member not found"))
                .when(memberCoSponsLegislationService).getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);

        mockMvc.perform(put("/api/member/{bioguideId}/cosponsored-legislation", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Member not found: Member not found"));

        Mockito.verify(memberCoSponsLegislationService, times(1))
                .getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);
    }

    @Test
    void testLoadCoSponsoredLegislationByMemberInternalServerError() throws Exception {
        String bioguideId = "A000360";

        Mockito.doThrow(new RuntimeException("Unexpected error"))
                .when(memberCoSponsLegislationService).getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);

        mockMvc.perform(put("/api/member/{bioguideId}/cosponsored-legislation", bioguideId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to load co-sponsored legislation for bioguideId: " + bioguideId));

        Mockito.verify(memberCoSponsLegislationService, times(1))
                .getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);
    }
}
