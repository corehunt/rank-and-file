package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.person.MembersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MembersController {

    public static final int LIMIT = 250;
    private final MembersService membersService;

    @Autowired

    public MembersController(MembersService membersService) {
        this.membersService = membersService;
    }

    /**
     * Loads all members in the below endpoint and saves it to the database.
     * api.congress.gov endpoint: /member
     *
     * @return A confirmation message or error response.
     */
    @PutMapping("/all")
    public ResponseEntity<String> loadAllMembers() {
        log.info("Starting to load all members");
        try {
            membersService.fetchAndSaveMembers(LIMIT);
            return ResponseEntity.ok("Successfully loaded and saved members");
        } catch (Exception e) {
            log.error("Failed to load members", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load all members: " + e.getMessage());
        }
    }
}
