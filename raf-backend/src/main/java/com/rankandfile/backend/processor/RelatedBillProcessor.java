package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class RelatedBillProcessor {

    private static final String FIELD_RELATED_BILLS = "relatedBills";
    private static final String FIELD_CONGRESS = "congress";
    private static final String FIELD_BILL_NUMBER = "number";
    private static final String FIELD_BILL_TYPE = "type";

    private final BillRepository billRepository;

    public RelatedBillProcessor(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill processRelatedBills(String json, Bill bill) {

        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();

        if (rootObject == null || !rootObject.has(FIELD_RELATED_BILLS)) {
            log.warn("No related bills found for Bill #: {}, returning bill", bill.getBillNo());
            return bill;
        }

        JsonArray relatedBillsArray = rootObject.getAsJsonArray(FIELD_RELATED_BILLS);
        if (relatedBillsArray == null || relatedBillsArray.isEmpty()) {
            log.warn("Related Bills array is empty for Bill #: {}, returning bill", bill.getBillNo());
            return bill;
        }

        Set<Bill> relatedBills = new HashSet<>();

        for (JsonElement element : relatedBillsArray) {
            if (element.isJsonNull()) {
                continue;
            }

            JsonObject relatedBillObj = element.getAsJsonObject();

            Bill relatedBill = extractBill(relatedBillObj);

            if (relatedBill == null) {
                continue;
            }

            relatedBills.add(relatedBill);
        }

        bill.setRelatedBills(relatedBills);
        log.info("successfully saved related bills to bill {} with {} related bills", bill.getBillId(), relatedBills.size());

        return bill;
    }

    private Bill extractBill(JsonObject relatedBillObj) {
        String congressNo = getAsString(relatedBillObj, FIELD_CONGRESS);
        String billType = getAsString(relatedBillObj, FIELD_BILL_TYPE);
        String billNumber = getAsString(relatedBillObj, FIELD_BILL_NUMBER);

        return billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

}
