package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.committee.CommitteeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/committee")
public class CommitteeController {

    private static final int LIMIT = 250;

    private final CommitteeService committeeService;

    public CommitteeController(CommitteeService committeeService) {
        this.committeeService = committeeService;
    }

    @PostMapping("/all")
    public ResponseEntity<String> getAllCommittees() {
        log.info("In Committee controller, loading all committees");
        committeeService.fetchAndProcessCommittees(LIMIT);
        return ResponseEntity.ok("Successfully loaded committees");
    }

}
