package com.rankandfile.dataloader.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

// This processor is used to load db with all bills from a given congress.
// The loop will run till all are processed.
@Slf4j
@Component
public class BillByCongressProcessor {

    private static final String FIELD_BILLS = "bills";
    private static final String FIELD_CONGRESS = "congress";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_LATEST_ACTION = "latestAction";
    private static final String FIELD_ACTION_DATE = "actionDate";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ORIGIN_CHAMBER = "originChamber";
    private static final String FIELD_ORIGIN_CHAMBER_CODE = "originChamberCode";

    private final IdGenerator idGenerator;
    private final BillRepository billRepository;

    public BillByCongressProcessor(IdGenerator idGenerator, BillRepository billRepository) {
        this.idGenerator = idGenerator;
        this.billRepository = billRepository;
    }

    /**
     * Processes the JSON string containing bills and returns a list of Bill entities.
     *
     * @param json The JSON string containing bills.
     * @return A list of Bill entities.
     */
    public List<Bill> processBillList(String json) {
        log.info("Starting processing bills from Congress JSON data");

        JsonObject responseObject = JsonParser.parseString(json).getAsJsonObject();
        if (responseObject == null || !responseObject.has(FIELD_BILLS)) {
            log.warn("No bills found in the provided JSON");
            return Collections.emptyList();
        }

        JsonElement billsElement = responseObject.get(FIELD_BILLS);
        if (billsElement == null || billsElement.isJsonNull()) {
            log.warn("Bills element is null in the provided JSON");
            return Collections.emptyList();
        }

        JsonArray billListArray = billsElement.getAsJsonArray();
        if (billListArray == null || billListArray.isEmpty()) {
            log.warn("Bills array is empty");
            return Collections.emptyList();
        }

        List<Bill> bills = new ArrayList<>();

        // Extract congress and bill numbers from the JSON
        Set<Integer> congressNumbers = new HashSet<>();
        Set<Integer> billNumbers = new HashSet<>();

        for (JsonElement element : billListArray) {
            JsonObject billObject = element.getAsJsonObject();
            Integer congressNo = getAsInteger(billObject, FIELD_CONGRESS);
            Integer billNo = getAsInteger(billObject, FIELD_NUMBER);

            if (congressNo != null && billNo != null) {
                congressNumbers.add(congressNo);
                billNumbers.add(billNo);
            }
        }

        // Retrieve existing bills from the database
        List<Bill> existingBills = billRepository.findByCongressInAndBillNoIn(congressNumbers, billNumbers);

        // Create a map of existing bills for quick lookup
        Map<String, Bill> existingBillMap = existingBills.stream()
                .collect(Collectors.toMap(
                        bill -> bill.getBillId(),
                        bill -> bill
                ));

        for (JsonElement element : billListArray) {
            JsonObject billObject = element.getAsJsonObject();

            String congressNo = getAsString(billObject, FIELD_CONGRESS);
            String billNo = getAsString(billObject, FIELD_NUMBER);
            String billType = getAsString(billObject, FIELD_TYPE);

            if (congressNo == null || billNo == null) {
                log.warn("Missing congress number or bill number in bill data. Skipping bill.");
                continue;
            }

            String billId = idGenerator.generateBillId(congressNo, billType, billNo);
            Bill bill = existingBillMap.get(billId);

            if (bill == null) {
                // New bill
                bill = new Bill();
                bill.setBillId(billId);
                bill.setBillNo(billNo);
                bill.setCongress(congressNo);

                log.info("Creating new Bill with ID: {}", bill.getBillId());

                // Extract and set bill properties
                extractBillFromJson(billObject, bill);

                bills.add(bill);
            } else {
                // Existing bill
                log.info("Bill already exists with ID: {}. Checking for updates.", bill.getBillId());

                // Check if the bill data has changed
                boolean isUpdated = updateExistingBill(billObject, bill);

                if (isUpdated) {
                    log.info("Bill ID: {} has been updated.", bill.getBillId());
                } else {
                    log.info("No changes detected for Bill ID: {}", bill.getBillId());
                }

                bills.add(bill);
            }
        }

        log.info("Completed processing {} bills", bills.size());

        return bills;
    }

    private boolean updateExistingBill(JsonObject billObject, Bill bill) {
        boolean isUpdated = false;

        // Check and update Bill Title
        String billTitle = getAsString(billObject, FIELD_TITLE);
        if (!Objects.equals(billTitle, bill.getBillTitle())) {
            bill.setBillTitle(billTitle);
            isUpdated = true;
        }

        // Process Latest Action
        if (billObject.has(FIELD_LATEST_ACTION) && !billObject.get(FIELD_LATEST_ACTION).isJsonNull()) {
            JsonObject latestActionObject = billObject.getAsJsonObject(FIELD_LATEST_ACTION);

            // Latest Action Date
            String latestActionDateString = getAsString(latestActionObject, FIELD_ACTION_DATE);
            LocalDate latestActionDate = null;
            if (latestActionDateString != null) {
                try {
                    latestActionDate = LocalDate.parse(latestActionDateString);
                } catch (DateTimeParseException e) {
                    log.error("Invalid date format for latestActionDate: {}", latestActionDateString, e);
                }
            }

            if (!Objects.equals(latestActionDate, bill.getLatestActionDt())) {
                bill.setLatestActionDt(latestActionDate);
                isUpdated = true;
            }

            // Latest Action Text
            String latestActionText = getAsString(latestActionObject, FIELD_TEXT);
            if (!Objects.equals(latestActionText, bill.getLatestActionTxt())) {
                bill.setLatestActionTxt(latestActionText);
                isUpdated = true;
            }
        }

        // Bill Type
        String billType = getAsString(billObject, FIELD_TYPE);
        if (!Objects.equals(billType, bill.getBillType())) {
            bill.setBillType(billType);
            isUpdated = true;
        }

        // Origin Chamber
        String originChamber = getAsString(billObject, FIELD_ORIGIN_CHAMBER);
        if (!Objects.equals(originChamber, bill.getOriginChamber())) {
            bill.setOriginChamber(originChamber);
            isUpdated = true;
        }

        // Origin Chamber Code
        String originChamberCode = getAsString(billObject, FIELD_ORIGIN_CHAMBER_CODE);
        if (!Objects.equals(originChamberCode, bill.getOriginChamberCd())) {
            bill.setOriginChamberCd(originChamberCode);
            isUpdated = true;
        }

        return isUpdated;
    }

    private void extractBillFromJson(JsonObject billObject, Bill bill) {
        // Set Bill Title
        String billTitle = getAsString(billObject, FIELD_TITLE);
        bill.setBillTitle(billTitle);

        // Process Latest Action
        if (billObject.has(FIELD_LATEST_ACTION) && !billObject.get(FIELD_LATEST_ACTION).isJsonNull()) {
            JsonObject latestActionObject = billObject.getAsJsonObject(FIELD_LATEST_ACTION);

            String latestActionDateString = getAsString(latestActionObject, FIELD_ACTION_DATE);
            if (latestActionDateString != null) {
                try {
                    LocalDate latestActionDate = LocalDate.parse(latestActionDateString);
                    bill.setLatestActionDt(latestActionDate);
                } catch (DateTimeParseException e) {
                    log.error("Invalid date format for latestActionDate: {}", latestActionDateString, e);
                    bill.setLatestActionDt(null);
                }
            }

            String latestActionText = getAsString(latestActionObject, FIELD_TEXT);
            bill.setLatestActionTxt(latestActionText);
        }

        // Set Bill Type
        String billType = getAsString(billObject, FIELD_TYPE);
        bill.setBillType(billType);

        // Set Origin Chamber
        String originChamber = getAsString(billObject, FIELD_ORIGIN_CHAMBER);
        bill.setOriginChamber(originChamber);

        // Set Origin Chamber Code
        String originChamberCode = getAsString(billObject, FIELD_ORIGIN_CHAMBER_CODE);
        bill.setOriginChamberCd(originChamberCode);
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
