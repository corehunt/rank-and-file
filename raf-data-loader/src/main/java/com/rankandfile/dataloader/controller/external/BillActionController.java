package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.bill.BillActionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bill")
public class BillActionController {

    private static final int LIMIT = 250;

    private final BillActionService billActionService;

    @Autowired
    public BillActionController(BillActionService billActionService) {
        this.billActionService = billActionService;
    }

    /**
     * Loads actions associated with a specific bill from the external API and saves them to the database.
     * api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}/actions
     *
     * @param congressNo The Congress number.
     * @param billType   The bill type.
     * @param billNumber The bill number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}/{billType}/{billNumber}/actions")
    public ResponseEntity<String> loadActionsForBillNumber(
            @PathVariable String congressNo,
            @PathVariable String billType,
            @PathVariable String billNumber) {
        log.info("Loading action data for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            billActionService.getActionsByBillNumber(congressNo, billType, billNumber, LIMIT);
            return ResponseEntity.ok("Successfully loaded actions for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading actions for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load actions for bill number: " + billNumber);
        }
    }
}
