package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.committee.CommitteeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/committee")
public class CommitteeController {

    private static final int LIMIT = 250;

    private final CommitteeService committeeService;

    @Autowired
    public CommitteeController(CommitteeService committeeService) {
        this.committeeService = committeeService;
    }

    /**
     * Loads all committees from the external API and saves them to the database.
     * api.congress.gov endpoint: /committee
     * @return A confirmation message.
     */
    @PutMapping("/all")
    public ResponseEntity<String> loadAllCommittees() {
        log.info("Loading all committees");
        try {
            committeeService.fetchAndProcessCommittees(LIMIT);
            return ResponseEntity.ok("Successfully loaded committees");
        } catch (Exception e) {
            log.error("Error loading committees: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load committees");
        }
    }
}
