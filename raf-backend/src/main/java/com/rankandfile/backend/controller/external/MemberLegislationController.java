package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.person.MemberCoSponsLegislationService;
import com.rankandfile.backend.service.external.person.MemberSponsLegislationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberLegislationController {

    private static final int LIMIT = 250;
    private final MemberSponsLegislationService memberSponsLegislationService;
    private final MemberCoSponsLegislationService memberCoSponsLegislationService;

    @Autowired
    public MemberLegislationController(
            MemberSponsLegislationService memberSponsLegislationService,
            MemberCoSponsLegislationService memberCoSponsLegislationService) {
        this.memberSponsLegislationService = memberSponsLegislationService;
        this.memberCoSponsLegislationService = memberCoSponsLegislationService;
    }

    /**
     * Loads sponsored legislation for a member from the external API and saves it to the database.
     * api.congress.gov endpoint: /member/{bioguideId}/sponsored-legislation
     *
     * @param bioguideId The bioguide ID of the member.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{bioguideId}/sponsored-legislation")
    public ResponseEntity<String> loadSponsoredLegislationByMember(@PathVariable String bioguideId) {
        log.info("Loading sponsored legislation for member with bioguideId: {}", bioguideId);
        try {
            memberSponsLegislationService.getSponsoredLegislationByPersonId(bioguideId, LIMIT);
            return ResponseEntity.ok("Successfully loaded sponsored legislation for bioguideId: " + bioguideId);
        } catch (EntityNotFoundException e) {
            log.error("Member not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading sponsored legislation for bioguideId {}: {}", bioguideId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load sponsored legislation for bioguideId: " + bioguideId);
        }
    }

    /**
     * Loads co-sponsored legislation for a member from the external API and saves it to the database.
     * api.congress.gov endpoint: /member/{bioguideId}/cosponsored-legislation
     *
     * @param bioguideId The bioguide ID of the member.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{bioguideId}/cosponsored-legislation")
    public ResponseEntity<String> loadCoSponsoredLegislationByMember(@PathVariable String bioguideId) {
        log.info("Loading co-sponsored legislation for member with bioguideId: {}", bioguideId);
        try {
            memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);
            return ResponseEntity.ok("Successfully loaded co-sponsored legislation for bioguideId: " + bioguideId);
        } catch (EntityNotFoundException e) {
            log.error("Member not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading co-sponsored legislation for bioguideId {}: {}", bioguideId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load co-sponsored legislation for bioguideId: " + bioguideId);
        }
    }
}
