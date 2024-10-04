package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.person.CongressClassPersonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberCongressController {

    private static final int LIMIT = 250;
    private final CongressClassPersonService congressClassPersonService;

    @Autowired
    public MemberCongressController(CongressClassPersonService congressClassPersonService) {
        this.congressClassPersonService = congressClassPersonService;
    }

    /**
     * Loads all members of a given Congress from the external API and saves them to the database.
     * api.congress.gov endpoint: /member/congress/{congress}
     *
     * @param congressId The Congress number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/congress/{congressId}")
    public ResponseEntity<String> loadMembersOfCongress(@PathVariable String congressId) {
        log.info("Loading and saving members of Congress: {}", congressId);
        try {
            congressClassPersonService.fetchMembersOfCurrentCongress(congressId, LIMIT);
            return ResponseEntity.ok("Successfully loaded and saved members of Congress: " + congressId);
        } catch (EntityNotFoundException e) {
            log.error("Congress not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Congress not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading members of Congress {}: {}", congressId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load members of Congress: " + congressId);
        }
    }
}
