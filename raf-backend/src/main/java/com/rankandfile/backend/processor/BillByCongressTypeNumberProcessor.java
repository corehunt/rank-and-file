package com.rankandfile.backend.processor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BillByCongressTypeNumberProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillByCongressTypeNumberProcessor.class);

    private final BillRepository billRepository;

    public BillByCongressTypeNumberProcessor(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill process(String json) {
        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();

        // Access the "bill" object
        JsonObject billObject = rootObject.getAsJsonObject("bill");

        // Extract the necessary fields
        Integer congressNo = billObject.get("congress").getAsInt();
        String billType = billObject.get("type").getAsString();
        Integer billNo = billObject.get("number").getAsInt();

        Bill billToProcess = billRepository.findByCongressAndBillNo(congressNo, billNo);

        String titleString = billObject.get("title").getAsString();
        if(billToProcess.getBillTitle() == null || !billToProcess.getBillTitle().equals(titleString)){
            billToProcess.setBillTitle(titleString);
        }

        String introDtString = billObject.get("introducedDate").getAsString();
        LocalDate introducedDt = LocalDate.parse(introDtString);

        if(billToProcess.getIntroducedDt() == null || !billToProcess.getIntroducedDt().equals(introducedDt)){
            billToProcess.setIntroducedDt(introducedDt);
        }

        JsonObject latestActionObject = billObject.getAsJsonObject("latestAction");
        if (latestActionObject != null) {
            String latestActionDate = latestActionObject.has("actionDate") && !latestActionObject.get("actionDate").isJsonNull() ? latestActionObject.get("actionDate").getAsString() : null;
            LocalDate actionDate = latestActionDate != null ? LocalDate.parse(latestActionDate) : null;
            String latestActionText = latestActionObject.has("text") && !latestActionObject.get("text").isJsonNull() ? latestActionObject.get("text").getAsString() : null;

            if(billToProcess.getLatestActionDt() == null || !billToProcess.getLatestActionDt().equals(actionDate)){
                billToProcess.setLatestActionDt(actionDate);
            }
            if(billToProcess.getLatestActionTxt() == null || !billToProcess.getLatestActionTxt().equals(latestActionText)){
                billToProcess.setLatestActionTxt(latestActionText);
            }
        }

        JsonObject policyAreaObject = billObject.getAsJsonObject("policyArea");
        if(policyAreaObject != null){
            String policyArea = policyAreaObject.get("name").getAsString();
            if(billToProcess.getPolicyArea() == null || !billToProcess.getPolicyArea().equals(policyArea)){
                billToProcess.setPolicyArea(policyArea);
            }
        }

        if(billToProcess.getOriginChamber() == null) {
            billToProcess.setOriginChamber(billObject.get("originChamber").getAsString());
        }

        if(billToProcess.getOriginChamberCd() == null) {
            billToProcess.setOriginChamberCd(billObject.get("originChamberCode").getAsString());
        }

        LOGGER.info("Processed bill from congress #: {}, bill type: {}, bill #: {}", congressNo, billType, billNo);
        return billToProcess;
    }
}
