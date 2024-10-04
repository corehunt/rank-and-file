package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.service.external.bill.BillByCongressService;
import com.rankandfile.backend.service.external.bill.BillByTypeAndNumberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bill")
public class BillController {

    private static final int LIMIT = 250;

    private final BillByCongressService billByCongressService;
    private final BillByTypeAndNumberService billByTypeAndNumberService;

    @Autowired
    public BillController(
            BillByCongressService billByCongressService,
            BillByTypeAndNumberService billByTypeAndNumberService) {
        this.billByCongressService = billByCongressService;
        this.billByTypeAndNumberService = billByTypeAndNumberService;
    }

    /**
     * Loads bills for a specific Congress session from the external API and saves them to the database.
     * api.congress.gov endpoint: /bill/{congress}
     *
     * @param congressNo The Congress number (e.g., 117).
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}")
    public ResponseEntity<String> loadBillsByCongress(
            @PathVariable Integer congressNo) {
        log.info("Loading bills for Congress number: {}", congressNo);
        try {
            billByCongressService.getBillsByCongress(congressNo, LIMIT);
            return ResponseEntity.ok("Successfully loaded bills for Congress number: " + congressNo);
        } catch (EntityNotFoundException e) {
            log.error("Congress not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Congress not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading bills for Congress number {}: {}", congressNo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load bills for Congress number: " + congressNo);
        }
    }

    /**
     * Loads detailed data for a specific bill from the external API and saves it to the database.
     * api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}
     *
     * @param congressNo The Congress number.
     * @param billType   The bill type (e.g., "hr", "s").
     * @param billNumber The bill number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}/{billType}/{billNumber}")
    public ResponseEntity<String> loadBillDataByTypeAndNumber(
            @PathVariable Integer congressNo,
            @PathVariable String billType,
            @PathVariable Integer billNumber) {
        log.info("Loading bill data for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            billByTypeAndNumberService.getBillByTypeAndNumber(congressNo, billType, billNumber);
            return ResponseEntity.ok("Successfully loaded data for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading bill data for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load data for bill number: " + billNumber);
        }
    }
}
