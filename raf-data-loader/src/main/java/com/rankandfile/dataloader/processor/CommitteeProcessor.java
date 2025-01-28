package com.rankandfile.dataloader.processor;

import com.google.gson.*;
import com.rankandfile.dataloader.entity.Committee;
import com.rankandfile.dataloader.repository.CommitteeRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CommitteeProcessor {

    private static final String FIELD_COMMITTEES = "committees";
    private static final String FIELD_CHAMBER = "chamber";
    private static final String FIELD_COMMITTEE_TYPE_CODE = "committeeTypeCode";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SYSTEM_CODE = "systemCode";
    private static final String FIELD_URL = "url";
    private static final String FIELD_PARENT = "parent";
    private static final String FIELD_SUBCOMMITTEES = "subcommittees";

    private final CommitteeRepository committeeRepository;
    private final IdGenerator idGenerator;

    private Map<String, Committee> processedCommittees = new HashMap<>();

    public CommitteeProcessor(CommitteeRepository committeeRepository, IdGenerator idGenerator) {
        this.committeeRepository = committeeRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Processes the JSON string containing committee data and updates or creates the corresponding Committee entities.
     *
     * @param json The JSON string containing the committee data.
     * @return A list of processed Committee entities.
     */
    @Transactional
    public List<Committee> process(String json) {
        log.info("Starting processing committees from JSON data");

        JsonObject rootObject;
        try {
            rootObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return Collections.emptyList();
        }

        if (rootObject == null || !rootObject.has(FIELD_COMMITTEES)) {
            log.warn("No committees found in the provided JSON");
            return Collections.emptyList();
        }

        JsonArray committeesArray = rootObject.getAsJsonArray(FIELD_COMMITTEES);
        List<Committee> committeeList = new ArrayList<>();

        for (JsonElement committeeElement : committeesArray) {
            if (committeeElement.isJsonObject()) {
                JsonObject committeeObject = committeeElement.getAsJsonObject();
                Committee committee = processCommitteeObject(committeeObject, null);
                if (committee != null) {
                    committeeList.add(committee);
                }
            }
        }

        log.info("Processed {} committees from JSON data", committeeList.size());
        return committeeList;
    }

    private Committee processCommitteeObject(JsonObject committeeObject, Committee parentCommittee) {
        String systemCode = getAsString(committeeObject, FIELD_SYSTEM_CODE);
        if (systemCode == null) {
            log.warn("Missing systemCode in committee data. Cannot process committee.");
            return null;
        }

        // Check if this committee has been processed already
        if (processedCommittees.containsKey(systemCode)) {
            Committee existingCommittee = processedCommittees.get(systemCode);
            // Ensure the parent is set correctly
            if (parentCommittee != null && existingCommittee.getParent() == null) {
                existingCommittee.setParent(parentCommittee);
            }
            return existingCommittee;
        }

        // Find existing committee by systemCode
        Committee committee = committeeRepository.findBySysCode(systemCode);
        if (committee == null) {
            // Create new committee
            committee = new Committee();
            committee.setCommitteeId(idGenerator.generateCommitteeId(systemCode));
            committee.setSysCode(systemCode);

            log.info("Creating new Committee with ID: {}", committee.getCommitteeId());
        } else {
            log.info("Updating existing Committee with ID: {}", committee.getCommitteeId());
        }

        committee.setParent(parentCommittee);

        // Update committee fields
        updateCommitteeFromJson(committeeObject, committee);

        // Add committee to cache
        processedCommittees.put(systemCode, committee);

        // Save committee after updating fields and processing parent committee
        committeeRepository.save(committee);

        // Process subcommittees if any
        if (committeeObject.has(FIELD_SUBCOMMITTEES)) {
            JsonArray subcommitteesArray = committeeObject.getAsJsonArray(FIELD_SUBCOMMITTEES);
            List<Committee> subCommittees = new ArrayList<>();
            for (JsonElement subcommitteeElement : subcommitteesArray) {
                if (subcommitteeElement.isJsonObject()) {
                    JsonObject subcommitteeObject = subcommitteeElement.getAsJsonObject();
                    Committee subcommittee = processCommitteeObject(subcommitteeObject, committee);
                    if (subcommittee != null) {
                        subCommittees.add(subcommittee);
                    }
                }
            }
            committee.setSubCommittees(subCommittees);

            // Optionally save the committee again after setting subcommittees
            committeeRepository.save(committee);
        }

        return committee;
    }

    private void updateCommitteeFromJson(JsonObject committeeObject, Committee committee) {
        // Chamber
        String chamber = getAsString(committeeObject, FIELD_CHAMBER);
        if (!Objects.equals(chamber, committee.getChamber())) {
            committee.setChamber(chamber);
        }

        // Committee Type Code
        String committeeTypeCode = getAsString(committeeObject, FIELD_COMMITTEE_TYPE_CODE);
        if (!Objects.equals(committeeTypeCode, committee.getCommTypeCd())) {
            committee.setCommTypeCd(committeeTypeCode);
        }

        // Name
        String name = getAsString(committeeObject, FIELD_NAME);
        if (!Objects.equals(name, committee.getCommName())) {
            committee.setCommName(name);
        }

        // URL
        String urlSrc = getAsString(committeeObject, FIELD_URL);
        if (!Objects.equals(urlSrc, committee.getUrlSrc())) {
            committee.setUrlSrc(urlSrc);
        }

        // Parent Committee
        if (committeeObject.has(FIELD_PARENT) && !committeeObject.get(FIELD_PARENT).isJsonNull()) {
            JsonObject parentObject = committeeObject.getAsJsonObject(FIELD_PARENT);
            String parentSystemCode = getAsString(parentObject, FIELD_SYSTEM_CODE);

            if (parentSystemCode != null) {
                Committee parentCommittee = processCommitteeObject(parentObject, null);
                if (parentCommittee != null) {
                    committee.setParent(parentCommittee);
                }
            }
        }
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }
}
