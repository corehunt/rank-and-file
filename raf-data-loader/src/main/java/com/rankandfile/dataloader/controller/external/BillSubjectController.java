package com.rankandfile.dataloader.controller.external;

import com.rankandfile.dataloader.service.external.bill.BillSubjectService;
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
public class BillSubjectController {

    private final BillSubjectService billSubjectService;

    @Autowired
    public BillSubjectController(BillSubjectService billSubjectService) {
        this.billSubjectService = billSubjectService;
    }

    @PutMapping("/{congressNo}/{billType}/{billNumber}/subjects")
    public ResponseEntity<String> loadBillSubjects(
            @PathVariable String congressNo,
            @PathVariable String billType,
            @PathVariable String billNumber) {
        log.info("Loading bill texts for bill number: {}, congress: {}", billNumber, congressNo);
        try {
            billSubjectService.fetchBillSubjects(congressNo, billType, billNumber);
            return ResponseEntity.ok("Successfully loaded bill subjects for bill number: " + billNumber);
        } catch (EntityNotFoundException e) {
            log.error("Bill not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bill not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error loading bill texts for bill number {}: {}", billNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to load bill texts for bill number: " + billNumber);
        }
    }

}
