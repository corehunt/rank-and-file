package com.rankandfile.dataloader.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.repository.PersonRepository;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import com.rankandfile.dataloader.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CoSponsoredLegislationProcessor {

    private static final String FIELD_COSPONSORS = "cosponsors";
    private static final String FIELD_CO_SPONSOR_TYPE= "Co-Sponsor";
    private static final String FIELD_BIOGUIDE = "bioguideId";

    private final SponsoredLegislationRepository sponsoredLegislationRepository;
    private final PersonRepository personRepository;
    private final IdGenerator idGenerator;

    public CoSponsoredLegislationProcessor(
            SponsoredLegislationRepository sponsoredLegislationRepository,
            PersonRepository personRepository,
            IdGenerator idGenerator) {
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
        this.personRepository = personRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Processes the new JSON response containing a "cosponsors" array and creates
     * SponsoredLegislation relationships for the given Bill.
     *
     * @param json The JSON string containing the cosponsors data.
     * @param bill The Bill entity that these cosponsors should be linked to.
     * @return A list of SponsoredLegislation relationships.
     */
    public List<SponsoredLegislation> process(String json, Bill bill) {
        log.info("Starting processing of cosponsor data for Bill ID: {}", bill.getBillId());
        JsonObject rootObject;
        try {
            rootObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return Collections.emptyList();
        }

        if (!rootObject.has(FIELD_COSPONSORS)) {
            log.warn("No cosponsors array found in the response.");
            return Collections.emptyList();
        }

        JsonArray cosponsorsArray = rootObject.getAsJsonArray(FIELD_COSPONSORS);
        if (cosponsorsArray == null || cosponsorsArray.isEmpty()) {
            log.warn("Cosponsors array is empty.");
            return Collections.emptyList();
        }

        List<SponsoredLegislation> sponsoredLegislations = new ArrayList<>();

        // Build a map of existing SponsoredLegislation for the given Bill to avoid duplicates.
        Map<String, SponsoredLegislation> existingLegislationMap = sponsoredLegislationRepository
                .findByBillBillIdAndSponsorType(bill.getBillId(), FIELD_CO_SPONSOR_TYPE)
                .stream()
                .collect(Collectors.toMap(
                        leg -> generateKey(bill.getCongress(), bill.getBillNo(), bill.getBillType(), FIELD_CO_SPONSOR_TYPE, leg.getPerson().getPersonId()),
                        leg -> leg
                ));

        for (JsonElement element : cosponsorsArray) {
            JsonObject cosponsorObj = element.getAsJsonObject();
            String cosponsorId = getAsString(cosponsorObj, FIELD_BIOGUIDE);
            if (cosponsorId == null) {
                log.warn("Cosponsor element missing bioguideId; skipping element: {}", cosponsorObj);
                continue;
            }

            // Look up the Person given the current person id
            Person cosponsor = personRepository.findPersonByPersonId(cosponsorId);
            if (cosponsor == null) {
                log.warn("Cosponsor with bioguideId {} not found; skipping.", cosponsorId);
                continue;
            }

            String key = generateKey(bill.getCongress(), bill.getBillNo(), bill.getBillType(), FIELD_CO_SPONSOR_TYPE, cosponsorId);
            SponsoredLegislation existing = existingLegislationMap.get(key);
            if (existing == null) {
                SponsoredLegislation sponsoredLegislation = new SponsoredLegislation();
                sponsoredLegislation.setSponLegId(idGenerator.generateSponsLegId());
                sponsoredLegislation.setPerson(cosponsor);
                sponsoredLegislation.setBill(bill);
                sponsoredLegislation.setSponsorType(FIELD_CO_SPONSOR_TYPE);
                sponsoredLegislations.add(sponsoredLegislation);
                // Update map to prevent duplicates within this processing run.
                existingLegislationMap.put(key, sponsoredLegislation);
            } else {
                log.info("SponsoredLegislation relationship already exists for Cosponsor ID: {}", cosponsorId);
                sponsoredLegislations.add(existing);
            }
        }

        log.info("Completed processing cosponsors for Bill ID: {}. Total relationships created/processed: {}", bill.getBillId(), sponsoredLegislations.size());
        return sponsoredLegislations;
    }

    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private String generateKey(String congress, String billNo, String billType, String sponsorType, String personId) {
        String billNoStr = (billNo != null) ? billNo : "unknownBillNo";
        String billTypeStr = (billType != null) ? billType : "unknownBillType";
        String sponsorTypeStr = (sponsorType != null) ? sponsorType : "unknownSponsorType";
        String personIdStr = (personId != null) ? personId : "unknownPersonId";
        return congress + "-" + billNoStr + "-" + billTypeStr + "-" + sponsorTypeStr + "-" + personIdStr;
    }
}