package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.util.IdGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SponsoredLegislationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsoredLegislationProcessor.class);

    private static final String FIELD_SPONSORED_LEGISLATION = "sponsoredLegislation";
    private static final String FIELD_CONGRESS = "congress";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_INTRODUCED_DATE = "introducedDate";
    private static final String FIELD_LATEST_ACTION = "latestAction";
    private static final String FIELD_ACTION_DATE = "actionDate";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_POLICY_AREA = "policyArea";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_URL = "url";

    @Autowired
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdGenerator idGenerator;

    /**
     * Processes sponsored legislation JSON data and associates it with a person.
     *
     * @param json     The JSON string containing sponsored legislation data.
     * @param personId The ID of the person sponsoring the legislation.
     * @return         A list of SponsoredLegislation entities.
     */
    public List<SponsoredLegislation> process(String json, String personId) {
        LOGGER.info("Starting processing of sponsored legislation for personId: {}", personId);

        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();
        if (rootObject == null || !rootObject.has(FIELD_SPONSORED_LEGISLATION)) {
            LOGGER.warn("No sponsored legislation found in the input JSON.");
            return Collections.emptyList();
        }

        JsonArray sponsoredLegislationArray = rootObject.getAsJsonArray(FIELD_SPONSORED_LEGISLATION);
        if (sponsoredLegislationArray == null || sponsoredLegislationArray.isEmpty()) {
            LOGGER.warn("Sponsored legislation array is empty.");
            return Collections.emptyList();
        }

        List <SponsoredLegislation> sponsoredLegislations = new ArrayList<>();

        Person existingMember = personRepository.findPersonByPersonId(personId);
        if (existingMember == null) {
            LOGGER.error("Person with ID {} not found.", personId);
            throw new EntityNotFoundException("Person with ID " + personId + " not found.");
        }

        // Collect all existing SponsoredLegislation for the person
        List<SponsoredLegislation> existingLegislations = sponsoredLegislationRepository.findByPersonPersonId(personId);

        // Create a map for quick lookup
        Map<String, SponsoredLegislation> existingLegislationMap = existingLegislations.stream()
                .collect(Collectors.toMap(
                        leg -> generateKey(leg.getCongress(), leg.getBillNo(), leg.getBillType()),
                        leg -> leg));

        // Loop through each piece of sponsored legislation
        for (JsonElement element : sponsoredLegislationArray) {
            JsonObject legislationObject = element.getAsJsonObject();

            Integer congressNo = getAsInteger(legislationObject, FIELD_CONGRESS);
            Integer billNo = getAsInteger(legislationObject, FIELD_NUMBER);
            String billType = getAsString(legislationObject, FIELD_TYPE);

            String key = generateKey(congressNo, billNo, billType);

            SponsoredLegislation legislationToProcess = existingLegislationMap.get(key);

            if (legislationToProcess == null) {
                LOGGER.info("Creating new SponsoredLegislation for Congress No: {}, Bill No: {}, Bill Type: {}", congressNo, billNo, billType);
                legislationToProcess = new SponsoredLegislation();
                legislationToProcess.setCongress(congressNo);
                legislationToProcess.setBillNo(billNo);
                legislationToProcess.setBillType(billType);
                legislationToProcess.setSponLegId(idGenerator.generateSponsLegId());
            } else {
                LOGGER.info("Updating existing SponsoredLegislation with ID: {}", legislationToProcess.getSponLegId());
            }

            // Set Person
            legislationToProcess.setPerson(existingMember);

            // Process the legislation data
            processLegislation(legislationObject, legislationToProcess);

            sponsoredLegislations.add(legislationToProcess);
        }

        LOGGER.info("Completed processing of sponsored legislation for personId: {}", personId);

        return sponsoredLegislations;
    }

    private void processLegislation(JsonObject legislationObject, SponsoredLegislation legislationToProcess) {
        // Set title
        String titleString = getAsString(legislationObject, FIELD_TITLE);
        legislationToProcess.setLegTitle(titleString);

        // Set Type
        String billType = getAsString(legislationObject, FIELD_TYPE);
        legislationToProcess.setBillType(billType);

        String introDtString = getAsString(legislationObject, FIELD_INTRODUCED_DATE);
        if (introDtString != null) {
            try {
                LocalDate introducedDt = LocalDate.parse(introDtString);
                legislationToProcess.setIntroDt(introducedDt);
            } catch (DateTimeParseException e) {
                LOGGER.error("Invalid date format for introducedDate: {}", introDtString, e);
                legislationToProcess.setIntroDt(null);
            }
        }

        // Process latest action if it is not null
        if (legislationObject.has(FIELD_LATEST_ACTION) && !legislationObject.get(FIELD_LATEST_ACTION).isJsonNull()) {
            JsonObject latestActionObject = legislationObject.getAsJsonObject(FIELD_LATEST_ACTION);
            String latestActionDate = getAsString(latestActionObject, FIELD_ACTION_DATE);
            if (latestActionDate != null) {
                try {
                    LocalDate actionDate = LocalDate.parse(latestActionDate);
                    legislationToProcess.setLatestActionDt(actionDate);
                } catch (DateTimeParseException e) {
                    LOGGER.error("Invalid date format for latestActionDate: {}", latestActionDate, e);
                    legislationToProcess.setLatestActionDt(null);
                }
            }
            String latestActionText = getAsString(latestActionObject, FIELD_TEXT);
            legislationToProcess.setLatestActionTxt(latestActionText);
        } else {
            legislationToProcess.setLatestActionDt(null);
            legislationToProcess.setLatestActionTxt(null);
        }

        // Process policy area if it is not null
        if (legislationObject.has(FIELD_POLICY_AREA) && !legislationObject.get(FIELD_POLICY_AREA).isJsonNull()) {
            JsonObject policyAreaObject = legislationObject.getAsJsonObject(FIELD_POLICY_AREA);
            String policyArea = getAsString(policyAreaObject, FIELD_NAME);
            legislationToProcess.setPolicyArea(policyArea);
        } else {
            legislationToProcess.setPolicyArea(null);
        }

        // Add URL source
        String url = getAsString(legislationObject, FIELD_URL);
        legislationToProcess.setUrlSrc(url);
    }


    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        String value = getAsString(obj, field);
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid number format for field {}: {}", field, value, e);
            return null;
        }
    }

    private String generateKey(Integer congressNo, Integer billNo, String billType) {
        return congressNo + "-" + billNo + "-" + billType;
    }
}