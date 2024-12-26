package com.rankandfile.backend.controller.internal;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.service.internal.BillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal")
@Controller("internalBillController")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<BillDTO> getBillForBillPage(@PathVariable String billId) {
        BillDTO billDTO = billService.getBillById(billId);
        return ResponseEntity.ok(billDTO);
    }

    @GetMapping("/bill/recent")
    public ResponseEntity<List<BillDTO>> getRecentBills() {
        List<BillDTO> recentBills = billService.getRecentBills();
        return ResponseEntity.ok(recentBills);
    }
}
