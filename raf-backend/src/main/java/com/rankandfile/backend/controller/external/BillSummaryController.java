package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.bill.BillSummaryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bill")
public class BillSummaryController {

    private final BillSummaryService billSummaryService;

    @Autowired
    public BillSummaryController(BillSummaryService billSummaryService) {
        this.billSummaryService = billSummaryService;
    }

    /**
     * Loads the latest summary for a specific bill from the external API and saves it to the database.
     * api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}/summaries
     *
     * @param congressNo The Congress number.
     * @param billType   The bill type.
     * @param billNumber The bill number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}/{billType}/{billNumber}/summary")
    public ResponseEntity<String> loadBillSummary(
            @PathVariable String congressNo,
            @PathVariable String billType,
            @PathVariable String billNumber) {
        log.info("Loading summary for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            billSummaryService.fetchBillSummary(congressNo, billType, billNumber);
            return ResponseEntity.ok("Successfully loaded summary for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading summary for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load summary for bill number: " + billNumber);
        }
    }
}
