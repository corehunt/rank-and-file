package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SponsoredLegislationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SponsoredLegislationProcessor.class);

    @Autowired
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdGenerator idGenerator;

    public List<SponsoredLegislation> process(String json, String personId) {
        LOGGER.info("Starting processing of sponsored legislation for personId: {}", personId);

        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();

        JsonArray sponsoredLegislationArray = rootObject.getAsJsonArray("sponsoredLegislation");

        List <SponsoredLegislation> sponsoredLegislations = new ArrayList<>();

        Person existingMember = personRepository.findPersonByPersonId(personId);

        // Collect all existing SponsoredLegislation for the person
        List<SponsoredLegislation> existingLegislations = sponsoredLegislationRepository.findByPersonPersonId(personId);

        // Create a map for quick lookup
        Map<String, SponsoredLegislation> existingLegislationMap = existingLegislations.stream()
                .collect(Collectors.toMap(
                        leg -> generateKey(leg.getCongress(), leg.getBillNo(), leg.getBillType()),
                        leg -> leg));

        // Loop through each piece of sponsored legislation
        for (int i = 0; i < sponsoredLegislationArray.size(); i++) {
            JsonObject legislationObject = sponsoredLegislationArray.get(i).getAsJsonObject();

            Integer congressNo = legislationObject.has("congress") && !legislationObject.get("congress").isJsonNull()
                    ? legislationObject.get("congress").getAsInt() : null;
            Integer billNo = legislationObject.has("number") && !legislationObject.get("number").isJsonNull()
                    ? Integer.parseInt(legislationObject.get("number").getAsString()) : null; // number is a string in JSON
            String billType = legislationObject.has("type") && !legislationObject.get("type").isJsonNull()
                    ? legislationObject.get("type").getAsString() : null;

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
        String titleString = legislationObject.has("title") && !legislationObject.get("title").isJsonNull()
                ? legislationObject.get("title").getAsString() : null;
        if (legislationToProcess.getLegTitle() == null || !legislationToProcess.getLegTitle().equals(titleString)) {
            legislationToProcess.setLegTitle(titleString);
        }

        // Set Type
        String billType = legislationObject.has("type") && !legislationObject.get("type").isJsonNull()
                ? legislationObject.get("type").getAsString() : null;
        legislationToProcess.setBillType(billType);

        // Set introduced date
        if (legislationObject.has("introducedDate") && !legislationObject.get("introducedDate").isJsonNull()) {
            String introDtString = legislationObject.get("introducedDate").getAsString();
            LocalDate introducedDt = LocalDate.parse(introDtString);
            if (legislationToProcess.getIntroDt() == null || !legislationToProcess.getIntroDt().equals(introducedDt)) {
                legislationToProcess.setIntroDt(introducedDt);
            }
        }

        // Process latest action
        if (legislationObject.has("latestAction") && !legislationObject.get("latestAction").isJsonNull()) {
            JsonObject latestActionObject = legislationObject.getAsJsonObject("latestAction");
            if (latestActionObject != null) {
                String latestActionDate = latestActionObject.has("actionDate") && !latestActionObject.get("actionDate").isJsonNull()
                        ? latestActionObject.get("actionDate").getAsString() : null;
                LocalDate actionDate = latestActionDate != null ? LocalDate.parse(latestActionDate) : null;
                String latestActionText = latestActionObject.has("text") && !latestActionObject.get("text").isJsonNull()
                        ? latestActionObject.get("text").getAsString() : null;

                if (legislationToProcess.getLatestActionDt() == null || !legislationToProcess.getLatestActionDt().equals(actionDate)) {
                    legislationToProcess.setLatestActionDt(actionDate);
                }
                if (legislationToProcess.getLatestActionTxt() == null || !legislationToProcess.getLatestActionTxt().equals(latestActionText)) {
                    legislationToProcess.setLatestActionTxt(latestActionText);
                }
            }
        }

        // Process policy area
        if (legislationObject.has("policyArea") && !legislationObject.get("policyArea").isJsonNull()) {
            JsonObject policyAreaObject = legislationObject.getAsJsonObject("policyArea");
            String policyArea = policyAreaObject.has("name") && !policyAreaObject.get("name").isJsonNull()
                    ? policyAreaObject.get("name").getAsString() : null;
            legislationToProcess.setPolicyArea(policyArea);
        }

        // Add url source
        String url = legislationObject.has("url") && !legislationObject.get("url").isJsonNull()
                ? legislationObject.get("url").getAsString() : null;
        legislationToProcess.setUrlSrc(url);
    }

    private String generateKey(Integer congressNo, Integer billNo, String billType) {
        return congressNo + "-" + billNo + "-" + billType;
    }
}