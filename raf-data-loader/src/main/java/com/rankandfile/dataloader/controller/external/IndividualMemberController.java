package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.person.IndividualMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class IndividualMemberController {

    private final IndividualMemberService individualMemberService;

    @Autowired
    public IndividualMemberController(IndividualMemberService individualMemberService) {
        this.individualMemberService = individualMemberService;
    }

    /**
     * Loads a single member based on bioguideId from the external API and saves it to the database.
     * api.congress.gov endpoint: /member/{bioguideId}
     *
     * @param bioguideId The bioguide ID of the member.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{bioguideId}")
    public ResponseEntity<String> loadMember(@PathVariable String bioguideId) {
        log.info("Loading and saving member with bioguideId: {}", bioguideId);
        try {
            individualMemberService.fetchAndProcessPerson(bioguideId);
            return ResponseEntity.ok("Successfully loaded and saved member with bioguideId: " + bioguideId);
        } catch (EntityNotFoundException e) {
            log.error("Member not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading member with bioguideId {}: {}", bioguideId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load member with bioguideId: " + bioguideId);
        }
    }
}
