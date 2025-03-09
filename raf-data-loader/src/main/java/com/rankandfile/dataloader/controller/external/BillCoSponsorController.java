package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.bill.BillCoSponsorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bill")
public class BillCoSponsorController {

    private final BillCoSponsorService billCoSponsorService;

    @Autowired
    public BillCoSponsorController(BillCoSponsorService billCoSponsorService) {
        this.billCoSponsorService = billCoSponsorService;
    }

    /**
     * Loads the cosponsors associated with a specific bill from the external API and saves them to the database.
     * api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}/cosponsors
     *
     * @param congressNo The Congress number.
     * @param billType   The bill type.
     * @param billNumber The bill number.
     * @return A confirmation message or error response.
     */
    @PutMapping("/{congressNo}/{billType}/{billNumber}/cosponsors")
    public ResponseEntity<String> loadCoSponsorsForBillNumber(
            @PathVariable String congressNo,
            @PathVariable String billType,
            @PathVariable String billNumber) {
        log.info("Loading co-sponsor data for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            billCoSponsorService.getCoSponsorsByBillNumber(congressNo, billType, billNumber);
            return ResponseEntity.ok("Successfully loaded co-sponsors for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading co-sponsors for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load co-sponsors for bill number: " + billNumber);
        }
    }
}
