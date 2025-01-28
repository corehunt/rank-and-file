package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.congress.CongressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/congress")
public class CongressController {

    private final CongressService congressService;

    @Autowired
    public CongressController(CongressService congressService) {
        this.congressService = congressService;
    }

    /**
     * Loads and saves Congress data and its sessions based on a specific Congress number.
     * api.congress.gov endpoint: /congress/{congress}
     *
     * @param congressNo The Congress number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}")
    public ResponseEntity<String> fetchAndSaveCongress(@PathVariable String congressNo) {
        log.info("Loading and saving Congress data for Congress number: {}", congressNo);
        try {
            congressService.fetchAndSaveCongressByNumber(congressNo);
            return ResponseEntity.ok("Successfully loaded Congress data for Congress number: " + congressNo);
        } catch (EntityNotFoundException e) {
            log.error("Congress not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Congress not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading Congress data for Congress number {}: {}", congressNo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load Congress data for Congress number: " + congressNo);
        }
    }
}
