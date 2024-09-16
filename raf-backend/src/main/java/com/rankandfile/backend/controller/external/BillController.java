package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.entity.Action;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.service.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bill")
public class BillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillController.class);

    private final BillService billService;

    @Autowired
    public BillController(BillService billService){
        this.billService = billService;
    }

    //This controller is used to load all bills by congress number
    //api.congress.gov endpoint: /bill/{congress}
    @GetMapping("/{congressNo}")
    public ResponseEntity<List<Bill>> getBillsByCongress(@PathVariable Integer congressNo){
        LOGGER.info("In Bill Controller, retrieving bill list for congress: {}", congressNo);
        List<Bill> billListByCongress = billService.getBillsByCongress(congressNo);
        return ResponseEntity.ok(billListByCongress);
    }

    //This controller is used to hydrate all bill data for a given bill
    //Payload needed from DB: CONGRESS - BILL_TYPE - BILL_NO
    //api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}
    @GetMapping("/{congressNo}/{billType}/{billNumber}")
    public ResponseEntity<Bill> getBillDataByTypeAndNumber(@PathVariable Integer congressNo, @PathVariable String billType, @PathVariable Integer billNumber){
        LOGGER.info("In Bill Controller, retrieving bill data for bill number: {}, during congress: {}", billNumber, congressNo);
        Bill bill = billService.getBillByTypeAndNumber(congressNo, billType, billNumber);
        return ResponseEntity.ok(bill);
    }

    //This controller is used to hydrate the actions for a specific bill, given the congress #, bill type
    //Payload needed from DB: CONGRESS - BILL_TYPE - BILL_NO
    //api.congress.gov endpoint: /bill/{congress}/{billType}/{billNumber}/actions
    @GetMapping("/{congressNo}/{billType}/{billNumber}/actions")
    public ResponseEntity<List<Action>> getActionsForBillNumber(@PathVariable Integer congressNo, @PathVariable String billType, @PathVariable Integer billNumber){
        LOGGER.info("In Bill Controller, retrieving action data for bill number: {}, during congress: {}", billNumber, congressNo);
        List<Action> actionsListByBill = billService.getActionsByBillNumber(congressNo, billType, billNumber);
        return ResponseEntity.ok(actionsListByBill);
    }
}
