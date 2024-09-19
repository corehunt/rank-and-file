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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SponsoredLegislationProcessor {

    private static final String FIELD_SPONSORED_LEGISLATION = "sponsoredLegislation";
    private static final String FIELD_COSPONSORED_LEGISLATION = "cosponsoredLegislation";
    private static final String FIELD_CONGRESS = "congress";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_URL = "url";

    private final SponsoredLegislationRepository sponsoredLegislationRepository;

    private final PersonRepository personRepository;

    private final BillRepository billRepository;

    private final BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    private final IdGenerator idGenerator;

    public SponsoredLegislationProcessor(SponsoredLegislationRepository sponsoredLegislationRepository, PersonRepository personRepository, BillRepository billRepository, BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor, IdGenerator idGenerator) {
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
        this.personRepository = personRepository;
        this.billRepository = billRepository;
        this.billByCongressTypeNumberProcessor = billByCongressTypeNumberProcessor;
        this.idGenerator = idGenerator;
    }

    /**
     * Processes sponsored or cosponsored legislation JSON data and associates it with a person.
     *
     * @param json     The JSON string containing sponsored or cosponsored legislation data.
     * @param personId The ID of the person sponsoring or cosponsoring the legislation.
     * @return A list of SponsoredLegislation entities.
     */
    public List<SponsoredLegislation> process(String json, String personId) {
        log.info("Starting processing of legislation for personId: {}", personId);

        JsonObject rootObject;
        try {
            rootObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return Collections.emptyList();
        }

        String sponsorType;
        JsonArray legislationArray;

        if (rootObject.has(FIELD_SPONSORED_LEGISLATION)) {
            sponsorType = "Sponsor";
            legislationArray = rootObject.getAsJsonArray(FIELD_SPONSORED_LEGISLATION);
        } else if (rootObject.has(FIELD_COSPONSORED_LEGISLATION)) {
            sponsorType = "Co-Sponsor";
            legislationArray = rootObject.getAsJsonArray(FIELD_COSPONSORED_LEGISLATION);
        } else {
            log.warn("No sponsored or cosponsored legislation found in the input JSON.");
            return Collections.emptyList();
        }

        if (legislationArray == null || legislationArray.isEmpty()) {
            log.warn("{} array is empty.", sponsorType);
            return Collections.emptyList();
        }

        List<SponsoredLegislation> sponsoredLegislations = new ArrayList<>();

        Person existingMember = personRepository.findPersonByPersonId(personId);
        if (existingMember == null) {
            log.error("Person with ID {} not found.", personId);
            throw new EntityNotFoundException("Person with ID " + personId + " not found.");
        }

        // Collect all existing SponsoredLegislation for the person and sponsorType
        List<SponsoredLegislation> existingLegislations = sponsoredLegislationRepository.findByPersonPersonIdAndSponsorType(personId, sponsorType);
        log.info("Found {} legislations for personId: {}", existingLegislations.size(), personId);

        // Create a map for quick lookup of existing SponsoredLegislation
        Map<String, SponsoredLegislation> existingLegislationMap = existingLegislations.stream()
                .collect(Collectors.toMap(
                        leg -> generateKey(leg.getBill().getCongress(), leg.getBill().getBillNo(), leg.getBill().getBillType()),
                        leg -> leg));

        // Process the legislation array
        for (JsonElement element : legislationArray) {
            JsonObject legislationObject = element.getAsJsonObject();

            // Filtering out amendments
            String urlSrc = getAsString(legislationObject, FIELD_URL);
            if (urlSrc != null && urlSrc.contains("/amendment/")) {
                log.info("Skipping amendment with URL: {}", urlSrc);
                continue;
            }

            Integer congressNo = getAsInteger(legislationObject, FIELD_CONGRESS);
            String billNoStr = getAsString(legislationObject, FIELD_NUMBER);
            String billType = getAsString(legislationObject, FIELD_TYPE);

            Integer billNo = parseBillNumber(billNoStr);

            if(billNo == null || congressNo == null) {
                continue;
            }

            String key = generateKey(congressNo, billNo, billType);

            // Fetch or create the Bill entity
            Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
            if (bill == null) {
                log.info("Creating new Bill for Congress No: {}, Bill No: {}, Bill Type: {}", congressNo, billNo, billType);
                bill = new Bill();
                bill.setBillId(idGenerator.generateBillId(congressNo, billNo));
                bill.setCongress(congressNo);
                bill.setBillNo(billNo);

                log.info("Creating new Bill with ID: {}", bill.getBillId());
                billByCongressTypeNumberProcessor.updateBillFromJson(legislationObject, bill);
                billRepository.save(bill);
            } else {
                log.info("Found existing Bill with ID: {}", bill.getBillId());
            }

            // Fetch or create the SponsoredLegislation entity
            SponsoredLegislation legislationToProcess = existingLegislationMap.get(key);

            if (legislationToProcess == null) {
                log.info("Creating new SponsoredLegislation for Bill ID: {} and Person ID: {}", bill.getBillId(), existingMember.getPersonId());
                legislationToProcess = new SponsoredLegislation();
                legislationToProcess.setSponLegId(idGenerator.generateSponsLegId());
                legislationToProcess.setPerson(existingMember);
                legislationToProcess.setBill(bill);
                legislationToProcess.setSponsorType(sponsorType);
                sponsoredLegislations.add(legislationToProcess);
            } else {
                log.info("SponsoredLegislation already exists with ID: {}", legislationToProcess.getSponLegId());
                // Update sponsorType if it has changed (unlikely in this context)
                if (!legislationToProcess.getSponsorType().equals(sponsorType)) {
                    legislationToProcess.setSponsorType(sponsorType);
                }
                sponsoredLegislations.add(legislationToProcess);
            }
        }

        log.info("Completed processing of legislation for personId: {}", personId);

        return sponsoredLegislations;
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        String value = getAsString(obj, field);
        return parseInteger(value);
    }

    private Integer parseInteger(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.error("Invalid number format: {}", value, e);
            return null;
        }
    }

    private Integer parseBillNumber(String billNoStr) {
        if (billNoStr == null) return null;
        // Remove any non-digit characters
        String digitsOnly = billNoStr.replaceAll("\\D+", "");
        return parseInteger(digitsOnly);
    }

    private String generateKey(Integer congressNo, Integer billNo, String billType) {
        String billNoStr = (billNo != null) ? billNo.toString() : "unknownBillNo";
        String billTypeStr = (billType != null) ? billType : "unknownBillType";
        return congressNo + "-" + billNoStr + "-" + billTypeStr;
    }
}