package com.rankandfile.backend.controller.internal;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.service.internal.BillSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal")
@Controller("internalBillSearchController")
public class BillSearchController {

    private final BillSearchService billSearchService;

    public BillSearchController(BillSearchService billSearchService) {
        this.billSearchService = billSearchService;
    }

    /**
     * Example: GET /api/internal/bill/search?q=tax+credit&page=0&size=10
     */
    @GetMapping("/bill/search")
    public ResponseEntity<List<BillDTO>> searchBills(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        log.info("Searching bills for query: {}", query);
        List<BillDTO> results = billSearchService.searchBills(query, page, size);
        return ResponseEntity.ok(results);
    }
}