package com.rankandfile.backend.processor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Slf4j
@Component
public class BillByCongressTypeNumberProcessor {

    private static final String FIELD_BILL = "bill";
    private static final String FIELD_CONGRESS = "congress";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_INTRODUCED_DATE = "introducedDate";
    private static final String FIELD_LATEST_ACTION = "latestAction";
    private static final String FIELD_ACTION_DATE = "actionDate";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ORIGIN_CHAMBER = "originChamber";
    private static final String FIELD_ORIGIN_CHAMBER_CODE = "originChamberCode";
    private static final String FIELD_POLICY_AREA = "policyArea";
    private static final String FIELD_NAME = "name";

    private final BillRepository billRepository;
    private final IdGenerator idGenerator;

    public BillByCongressTypeNumberProcessor(BillRepository billRepository, IdGenerator idGenerator) {
        this.billRepository = billRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Processes the JSON string containing a bill and updates or creates the corresponding Bill entity.
     *
     * @param json The JSON string containing the bill data.
     * @return The processed Bill entity.
     */
    public Bill process(String json) {
        log.info("Starting processing a single bill from JSON data");

        JsonObject rootObject;
        try {
            rootObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return null;
        }

        if (rootObject == null || !rootObject.has(FIELD_BILL)) {
            log.warn("No bill found in the provided JSON");
            return null;
        }

        JsonObject billObject = rootObject.getAsJsonObject(FIELD_BILL);
        if (billObject == null) {
            log.warn("Bill object is null in the provided JSON");
            return null;
        }

        Integer congressNo = getAsInteger(billObject, FIELD_CONGRESS);
        Integer billNo = getAsInteger(billObject, FIELD_NUMBER);

        if (congressNo == null || billNo == null) {
            log.warn("Missing congress number or bill number in bill data. Cannot process bill.");
            return null;
        }

        Bill bill = billRepository.findByCongressAndBillNo(congressNo, billNo);
        if (bill == null) {
            // Create new bill
            bill = new Bill();
            bill.setBillId(idGenerator.generateBillId(congressNo, billNo));
            bill.setCongress(congressNo);
            bill.setBillNo(billNo);

            log.info("Creating new Bill with ID: {}", bill.getBillId());
        } else {
            log.info("Updating existing Bill with ID: {}", bill.getBillId());
        }

        // Update bill fields
        updateBillFromJson(billObject, bill);

        log.info("Processed bill from congress #: {}, bill type: {}, bill #: {}", congressNo, bill.getBillType(), billNo);
        return bill;
    }

    private void updateBillFromJson(JsonObject billObject, Bill bill) {
        // Bill Title
        String billTitle = getAsString(billObject, FIELD_TITLE);
        if (!Objects.equals(billTitle, bill.getBillTitle())) {
            bill.setBillTitle(billTitle);
        }

        // Introduced Date
        String introDateString = getAsString(billObject, FIELD_INTRODUCED_DATE);
        if (introDateString != null) {
            try {
                LocalDate introducedDate = LocalDate.parse(introDateString);
                if (!Objects.equals(introducedDate, bill.getIntroducedDt())) {
                    bill.setIntroducedDt(introducedDate);
                }
            } catch (DateTimeParseException e) {
                log.error("Invalid date format for introducedDate: {}", introDateString, e);
            }
        }

        // Latest Action
        if (billObject.has(FIELD_LATEST_ACTION) && !billObject.get(FIELD_LATEST_ACTION).isJsonNull()) {
            JsonObject latestActionObject = billObject.getAsJsonObject(FIELD_LATEST_ACTION);

            // Latest Action Date
            String latestActionDateString = getAsString(latestActionObject, FIELD_ACTION_DATE);
            if (latestActionDateString != null) {
                try {
                    LocalDate latestActionDate = LocalDate.parse(latestActionDateString);
                    if (!Objects.equals(latestActionDate, bill.getLatestActionDt())) {
                        bill.setLatestActionDt(latestActionDate);
                    }
                } catch (DateTimeParseException e) {
                    log.error("Invalid date format for latestActionDate: {}", latestActionDateString, e);
                }
            }

            // Latest Action Text
            String latestActionText = getAsString(latestActionObject, FIELD_TEXT);
            if (!Objects.equals(latestActionText, bill.getLatestActionTxt())) {
                bill.setLatestActionTxt(latestActionText);
            }
        }

        // Policy Area
        if (billObject.has(FIELD_POLICY_AREA) && !billObject.get(FIELD_POLICY_AREA).isJsonNull()) {
            JsonObject policyAreaObject = billObject.getAsJsonObject(FIELD_POLICY_AREA);
            String policyArea = getAsString(policyAreaObject, FIELD_NAME);
            if (!Objects.equals(policyArea, bill.getPolicyArea())) {
                bill.setPolicyArea(policyArea);
            }
        }

        // Origin Chamber
        String originChamber = getAsString(billObject, FIELD_ORIGIN_CHAMBER);
        if (!Objects.equals(originChamber, bill.getOriginChamber())) {
            bill.setOriginChamber(originChamber);
        }

        // Origin Chamber Code
        String originChamberCode = getAsString(billObject, FIELD_ORIGIN_CHAMBER_CODE);
        if (!Objects.equals(originChamberCode, bill.getOriginChamberCd())) {
            bill.setOriginChamberCd(originChamberCode);
        }

        // Bill Type
        String billType = getAsString(billObject, FIELD_TYPE);
        if (!Objects.equals(billType, bill.getBillType())) {
            bill.setBillType(billType);
        }
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        String value = getAsString(obj, field);
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.error("Invalid number format for field '{}': {}", field, value, e);
            return null;
        }
    }
}
