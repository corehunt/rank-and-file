package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.util.IdGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SponsoredLegislationProcessor {

    private static final String FIELD_SPONSORED_LEGISLATION = "sponsoredLegislation";
    private static final String FIELD_COSPONSORED_LEGISLATION = "cosponsoredLegislation";
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
    private BillRepository billRepository;

    @Autowired
    private BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

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
        log.info("Starting processing of sponsored legislation for personId: {}", personId);

        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();
        if (rootObject == null || (!rootObject.has(FIELD_SPONSORED_LEGISLATION) && !rootObject.has(FIELD_COSPONSORED_LEGISLATION))) {
            log.warn("No sponsored or cosponsored legislation found in the input JSON.");
            return Collections.emptyList();
        }


        JsonArray arrayToProcess = new JsonArray();

        //Sponsored legislation null check and assignment
        JsonArray sponsoredLegislationArray = rootObject.getAsJsonArray(FIELD_SPONSORED_LEGISLATION);
        if (sponsoredLegislationArray == null || sponsoredLegislationArray.isEmpty()) {
            log.warn("Sponsored legislation array is empty.");
        } else {
            arrayToProcess = sponsoredLegislationArray;
        }

        //CoSponsored legislation null check and assignment
        JsonArray coSponsoredLegislation = rootObject.getAsJsonArray(FIELD_COSPONSORED_LEGISLATION);
        if(coSponsoredLegislation == null || coSponsoredLegislation.isEmpty()) {
            log.warn("Cosponsored legislation array is empty.");
        } else {
            arrayToProcess = coSponsoredLegislation;
        }

        List <SponsoredLegislation> sponsoredLegislations = new ArrayList<>();

        Person existingMember = personRepository.findPersonByPersonId(personId);
        if (existingMember == null) {
            log.error("Person with ID {} not found.", personId);
            throw new EntityNotFoundException("Person with ID " + personId + " not found.");
        }

        // Collect all existing SponsoredLegislation for the person
        List<SponsoredLegislation> existingLegislations = sponsoredLegislationRepository.findByPersonPersonId(personId);

        // Create a map for quick lookup of existing SponsoredLegislation
        Map<String, SponsoredLegislation> existingLegislationMap = existingLegislations.stream()
                .collect(Collectors.toMap(
                        leg -> generateKey(leg.getBill().getCongress(), leg.getBill().getBillNo(), leg.getBill().getBillType()),
                        leg -> leg));

        // Loop through each piece of sponsored legislation
        for (JsonElement element : arrayToProcess) {
            JsonObject legislationObject = element.getAsJsonObject();

            //Filtering out amendments
            String urlSrc = getAsString(legislationObject, FIELD_URL);
            if (urlSrc != null && urlSrc.contains("/amendment/")) {
                log.info("Skipping amendment with URL: {}", urlSrc);
                continue;
            }

            Integer congressNo = getAsInteger(legislationObject, FIELD_CONGRESS);
            Integer billNo = getAsInteger(legislationObject, FIELD_NUMBER);
            String billType = getAsString(legislationObject, FIELD_TYPE);
            String sponsor = getAsString(legislationObject, FIELD_SPONSORED_LEGISLATION);
            String coSponsor = getAsString(legislationObject, FIELD_COSPONSORED_LEGISLATION);

            String key = generateKey(congressNo, billNo, billType);

            // Fetch or create the Bill entity
            Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
            if (bill == null) {
                log.info("Creating new Bill for Congress No: {}, Bill No: {}, Bill Type: {}", congressNo, billNo, billType);
                bill = billByCongressTypeNumberProcessor.process(json);
            } else {
                log.info("Updating existing Bill with ID: {}", bill.getBillId());
            }

            SponsoredLegislation legislationToProcess = existingLegislationMap.get(key);

            if (legislationToProcess == null) {
                log.info("Creating new SponsoredLegislation for Congress No: {}, Bill No: {}, Bill Type: {}", congressNo, billNo, billType);
                legislationToProcess = new SponsoredLegislation();
                legislationToProcess.setSponLegId(idGenerator.generateSponsLegId());
            } else {
                log.info("Updating existing SponsoredLegislation with ID: {}", legislationToProcess.getSponLegId());
            }

            // Set Person
            legislationToProcess.setPerson(existingMember);

            // Set Bill
            legislationToProcess.setBill(bill);

            // Set sponsor type
            if(sponsor.equalsIgnoreCase(FIELD_SPONSORED_LEGISLATION)) {
                legislationToProcess.setSponsorType("Sponsor");
            } else if(coSponsor.equalsIgnoreCase(FIELD_COSPONSORED_LEGISLATION)) {
                legislationToProcess.setSponsorType("Co-Sponsor");
            }

            sponsoredLegislations.add(legislationToProcess);
        }

        log.info("Completed processing of sponsored legislation for personId: {}", personId);

        return sponsoredLegislations;
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        String value = getAsString(obj, field);
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.error("Invalid number format for field {}: {}", field, value, e);
            return null;
        }
    }

    private String generateKey(Integer congressNo, Integer billNo, String billType) {
        String billNoStr = (billNo != null) ? billNo.toString() : "unknownBillNo";
        String billTypeStr = (billType != null) ? billType : "unknownBillType";
        return congressNo + "-" + billNoStr + "-" + billTypeStr;
    }
}