package com.rankandfile.dataloader.processor;

import com.google.gson.*;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Committee;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.repository.CommitteeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class BillCommitteeProcessor {

    private static final String FIELD_COMMITTEES = "committees";
    private static final String FIELD_SYSTEM_CODE = "systemCode";
    private static final String FIELD_SUBCOMMITTEES = "subcommittees";

    private final CommitteeRepository committeeRepository;
    private final BillRepository billRepository;

    public BillCommitteeProcessor(CommitteeRepository committeeRepository, BillRepository billRepository) {
        this.committeeRepository = committeeRepository;
        this.billRepository = billRepository;
    }

    /**
     * Processes the JSON response containing committees associated with a bill and updates the bill's committee relationships.
     *
     * @param json The JSON response containing committees.
     * @param billId The ID of the bill to associate committees with.
     * @return The updated Bill entity.
     */
    @Transactional
    public Bill process(String json, String billId) {
        log.info("Starting processing committees for Bill ID: {}", billId);

        Optional<Bill> optionalBill = billRepository.findById(billId);
        if (optionalBill.isEmpty()) {
            log.warn("Bill with ID {} not found", billId);
            return null;
        }

        Bill bill = optionalBill.get();

        JsonObject rootObject;
        try {
            rootObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return bill;
        }

        if (rootObject == null || !rootObject.has(FIELD_COMMITTEES)) {
            log.warn("No committees found in the provided JSON");
            return bill;
        }

        JsonArray committeesArray = rootObject.getAsJsonArray(FIELD_COMMITTEES);
        Set<Committee> committees = new HashSet<>();

        for (JsonElement committeeElement : committeesArray) {
            if (committeeElement.isJsonObject()) {
                JsonObject committeeObject = committeeElement.getAsJsonObject();
                processCommitteeObject(committeeObject, committees);
            }
        }

        // Update the bill's committees
        bill.setCommittees(new ArrayList<>(committees));

        log.info("Updated and returning Bill with ID {} and {} committees", billId, committees.size());

        return bill;
    }

    private void processCommitteeObject(JsonObject committeeObject, Set<Committee> committees) {
        String systemCode = getAsString(committeeObject, FIELD_SYSTEM_CODE);
        if (systemCode == null) {
            log.warn("Missing systemCode in committee data. Cannot process committee.");
            return;
        }

        Committee committee = committeeRepository.findBySysCode(systemCode);
        if (committee == null) {
            log.warn("Committee with sysCode {} not found", systemCode);
            return;
        }

        // Add the committee to the set
        committees.add(committee);

        // Process subcommittees if any
        if (committeeObject.has(FIELD_SUBCOMMITTEES)) {
            JsonArray subcommitteesArray = committeeObject.getAsJsonArray(FIELD_SUBCOMMITTEES);
            for (JsonElement subcommitteeElement : subcommitteesArray) {
                if (subcommitteeElement.isJsonObject()) {
                    JsonObject subcommitteeObject = subcommitteeElement.getAsJsonObject();
                    processCommitteeObject(subcommitteeObject, committees);
                }
            }
        }
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }
}
