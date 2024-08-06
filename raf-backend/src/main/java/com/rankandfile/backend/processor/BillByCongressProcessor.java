package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.util.IdGenerator;
import com.rankandfile.backend.util.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// This processor is used to load db with all bills from a given congress.
// The loop will run till all are processed.

@Component
public class BillByCongressProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillByCongressProcessor.class);

    private final IdGenerator idGenerator;

    private Supplier billSupplier;

    public BillByCongressProcessor(IdGenerator idGenerator, Supplier billSupplier) {
        this.idGenerator = idGenerator;
        this.billSupplier = billSupplier;
    }

    public List<Bill> processBillList(String json){
        JsonObject responseObject = JsonParser.parseString(json).getAsJsonObject();

        JsonArray billListObject = responseObject.has("bills") && responseObject.get("bills").isJsonArray() ?
                responseObject.getAsJsonArray("bills") : new JsonArray();

        List<Bill> bills = new ArrayList<>();

        for (int i = 0; i < billListObject.size(); i++) {
            JsonObject billObject = billListObject.get(i).getAsJsonObject();
            Bill bill = extractBillFromJsonList(billObject);
            bills.add(bill);
        }

        return bills;
    }


    private Bill extractBillFromJsonList(JsonObject billObject){
        Integer congressNo = billObject.get("congress").getAsInt();
        Integer billNo = billObject.get("number").getAsInt();

        LOGGER.info("Extracting bill to jpa obj: {}", billNo);

        Bill bill = billSupplier.findOrCreateBill(congressNo, billNo);

        bill.setBillId(idGenerator.generateBillId(congressNo, billNo));
        bill.setBillNo(billNo);

        // Handle potentially missing fields
        bill.setBillTitle(billObject.has("title") && !billObject.get("title").isJsonNull() ?
                billObject.get("title").getAsString() : null);

        if (billObject.has("latestAction") && !billObject.get("latestAction").isJsonNull()) {
            JsonObject latestActionObject = billObject.getAsJsonObject("latestAction");

            String latestActionDate = latestActionObject.has("actionDate") && !latestActionObject.get("actionDate").isJsonNull() ?
                    latestActionObject.get("actionDate").getAsString() : null;
            LocalDate actionDate = latestActionDate != null ? LocalDate.parse(latestActionDate) : null;
            bill.setLatestActionDt(actionDate);

            String latestActionText = latestActionObject.has("text") && !latestActionObject.get("text").isJsonNull() ?
                    latestActionObject.get("text").getAsString() : null;
            bill.setLatestActionTxt(latestActionText);
        }

        bill.setCongress(congressNo);
        bill.setBillType(billObject.has("type") && !billObject.get("type").isJsonNull() ?
                billObject.get("type").getAsString() : null);
        bill.setOriginChamber(billObject.has("originChamber") && !billObject.get("originChamber").isJsonNull() ?
                billObject.get("originChamber").getAsString() : null);
        bill.setOriginChamberCd(billObject.has("originChamberCode") && !billObject.get("originChamberCode").isJsonNull() ?
                billObject.get("originChamberCode").getAsString() : null);

        return bill;
    }


}
