package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.bill.RelatedBillService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bill")
public class RelatedBillController {

    private final RelatedBillService relatedBillService;

    public RelatedBillController(RelatedBillService relatedBillService) {
        this.relatedBillService = relatedBillService;
    }

    /**
     * Loads related bills for a specific Bill Number from the external API and saves them to the database.
     * api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}/relatedbills
     *
     * @param congressNo The Congress number.
     * @param billType   The bill type.
     * @param billNumber The bill number.
     * @return A confirmation message or error response.
     */

    @PutMapping("/{congressNo}/{billType}/{billNumber}/relatedBills")
    public ResponseEntity<String> loadRelatedBills(
            @PathVariable String congressNo,
            @PathVariable String billType,
            @PathVariable String billNumber) {
        log.info("Loading related bill data for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            relatedBillService.getRelatedBills(congressNo, billType, billNumber);
            return ResponseEntity.ok("Successfully loaded related bills for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading related bills for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load related bills for bill number: " + billNumber);
        }
    }
}
