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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This processor processes a JSON response containing recent bills.
 * For each bill record, it queries the database using
 * {@code findByCongressAndBillNoAndBillType} and either updates the found record or creates a new one.
 */
@Slf4j
@Component
public class RecentBillProcessor {

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

    public RecentBillProcessor(IdGenerator idGenerator, BillRepository billRepository) {
        this.idGenerator = idGenerator;
        this.billRepository = billRepository;
    }

    /**
     * Processes the JSON response string containing recent bills and returns a list of Bill entities.
     * For each bill record, the processor queries the database based on congress, bill number, and bill type.
     * If a matching bill exists, it is updated; otherwise, a new Bill is created.
     *
     * @param json The JSON response string.
     * @return A list of processed Bill entities.
     */
    public List<Bill> processRecentBills(String json) {
        log.info("Starting processing recent bills from JSON data");

        JsonObject responseObject = JsonParser.parseString(json).getAsJsonObject();
        if (responseObject == null || !responseObject.has(FIELD_BILLS)) {
            log.warn("No bills found in the provided JSON");
            return new ArrayList<>();
        }

        JsonElement billsElement = responseObject.get(FIELD_BILLS);
        if (billsElement == null || billsElement.isJsonNull()) {
            log.warn("Bills element is null in the provided JSON");
            return new ArrayList<>();
        }

        JsonArray billListArray = billsElement.getAsJsonArray();
        if (billListArray == null || billListArray.isEmpty()) {
            log.warn("Bills array is empty");
            return new ArrayList<>();
        }

        List<Bill> processedBills = new ArrayList<>();

        for (JsonElement element : billListArray) {
            JsonObject billObject = element.getAsJsonObject();

            String congress = getAsString(billObject, FIELD_CONGRESS);
            String billNo = getAsString(billObject, FIELD_NUMBER);
            String billType = getAsString(billObject, FIELD_TYPE);

            if (congress == null || billNo == null || billType == null) {
                log.warn("Missing required fields (congress, number, or type) for a bill. Skipping record.");
                continue;
            }

            // Query the database using the repository method.
            Bill bill = billRepository.findByCongressAndBillNoAndBillType(congress, billNo, billType);

            if (bill == null) {
                // No existing bill found: create a new one.
                bill = new Bill();
                bill.setBillId(idGenerator.generateBillId(congress, billType, billNo));
                bill.setBillNo(billNo);
                bill.setCongress(congress);
                bill.setBillType(billType);
                log.info("Creating new Bill with ID: {}", bill.getBillId());

                extractBillFromJson(billObject, bill);
            } else {
                // Existing bill found: update if necessary.
                log.info("Found existing Bill with ID: {}. Checking for updates.", bill.getBillId());
                boolean updated = updateExistingBill(billObject, bill);
                if (updated) {
                    log.info("Bill {} updated.", bill.getBillId());
                } else {
                    log.info("No changes detected for Bill {}.", bill.getBillId());
                }
            }

            processedBills.add(bill);
        }

        log.info("Completed processing {} bills", processedBills.size());
        return processedBills;
    }

    /**
     * Checks and updates an existing Bill entity with data from the JSON object.
     *
     * @param billObject The JSON object representing the bill.
     * @param bill       The existing Bill entity.
     * @return true if any field was updated; false otherwise.
     */
    private boolean updateExistingBill(JsonObject billObject, Bill bill) {
        boolean isUpdated = false;

        // Update bill title if it has changed.
        String billTitle = getAsString(billObject, FIELD_TITLE);
        if (!Objects.equals(billTitle, bill.getBillTitle())) {
            bill.setBillTitle(billTitle);
            isUpdated = true;
        }

        // Process latest action if available.
        if (billObject.has(FIELD_LATEST_ACTION) && !billObject.get(FIELD_LATEST_ACTION).isJsonNull()) {
            JsonObject latestActionObject = billObject.getAsJsonObject(FIELD_LATEST_ACTION);
            String latestActionDateStr = getAsString(latestActionObject, FIELD_ACTION_DATE);
            LocalDate latestActionDate = null;
            if (latestActionDateStr != null) {
                try {
                    latestActionDate = LocalDate.parse(latestActionDateStr);
                } catch (DateTimeParseException e) {
                    log.error("Invalid latestAction date format: {}", latestActionDateStr, e);
                }
            }
            if (!Objects.equals(latestActionDate, bill.getLatestActionDt())) {
                bill.setLatestActionDt(latestActionDate);
                isUpdated = true;
            }
            String latestActionText = getAsString(latestActionObject, FIELD_TEXT);
            if (!Objects.equals(latestActionText, bill.getLatestActionTxt())) {
                bill.setLatestActionTxt(latestActionText);
                isUpdated = true;
            }
        }

        // Update origin chamber information.
        String originChamber = getAsString(billObject, FIELD_ORIGIN_CHAMBER);
        if (!Objects.equals(originChamber, bill.getOriginChamber())) {
            bill.setOriginChamber(originChamber);
            isUpdated = true;
        }
        String originChamberCode = getAsString(billObject, FIELD_ORIGIN_CHAMBER_CODE);
        if (!Objects.equals(originChamberCode, bill.getOriginChamberCd())) {
            bill.setOriginChamberCd(originChamberCode);
            isUpdated = true;
        }

        return isUpdated;
    }

    /**
     * Extracts bill data from the given JSON object and populates the Bill entity.
     *
     * @param billObject The JSON object representing the bill.
     * @param bill       The Bill entity to populate.
     */
    private void extractBillFromJson(JsonObject billObject, Bill bill) {
        // Set bill title.
        bill.setBillTitle(getAsString(billObject, FIELD_TITLE));

        // Process latest action if available.
        if (billObject.has(FIELD_LATEST_ACTION) && !billObject.get(FIELD_LATEST_ACTION).isJsonNull()) {
            JsonObject latestActionObject = billObject.getAsJsonObject(FIELD_LATEST_ACTION);
            String latestActionDateStr = getAsString(latestActionObject, FIELD_ACTION_DATE);
            if (latestActionDateStr != null) {
                try {
                    LocalDate latestActionDate = LocalDate.parse(latestActionDateStr);
                    bill.setLatestActionDt(latestActionDate);
                } catch (DateTimeParseException e) {
                    log.error("Invalid latestAction date format: {}", latestActionDateStr, e);
                    bill.setLatestActionDt(null);
                }
            }
            bill.setLatestActionTxt(getAsString(latestActionObject, FIELD_TEXT));
        }

        // Set origin chamber information.
        bill.setOriginChamber(getAsString(billObject, FIELD_ORIGIN_CHAMBER));
        bill.setOriginChamberCd(getAsString(billObject, FIELD_ORIGIN_CHAMBER_CODE));
    }

    /**
     * Safely retrieves the value of the given field as a String from a JSON object.
     *
     * @param obj   The JSON object.
     * @param field The field name.
     * @return The field value as a String, or null if not available.
     */
    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }
}
