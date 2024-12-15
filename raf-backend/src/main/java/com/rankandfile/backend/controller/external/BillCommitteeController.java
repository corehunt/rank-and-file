package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.bill.BillCommitteeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bill")
public class BillCommitteeController {

    private final BillCommitteeService billCommitteeService;

    @Autowired
    public BillCommitteeController(BillCommitteeService billCommitteeService) {
        this.billCommitteeService = billCommitteeService;
    }

    /**
     * Loads the committees associated with a specific bill from the external API and saves them to the database.
     * api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}/committees
     *
     * @param congressNo The Congress number.
     * @param billType   The bill type.
     * @param billNumber The bill number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}/{billType}/{billNumber}/committees")
    public ResponseEntity<String> loadCommitteesForBillNumber(
            @PathVariable String congressNo,
            @PathVariable String billType,
            @PathVariable String billNumber) {
        log.info("Loading committee data for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            billCommitteeService.getCommitteesByBillNumber(congressNo, billType, billNumber);
            return ResponseEntity.ok("Successfully loaded committees for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading committees for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load committees for bill number: " + billNumber);
        }
    }
}
