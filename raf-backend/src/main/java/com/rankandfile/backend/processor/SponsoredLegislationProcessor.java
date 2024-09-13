package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SponsoredLegislationProcessor {

    @Autowired
    private SponsoredLegislationRepository sponsoredLegislationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdGenerator idGenerator;

    public List<SponsoredLegislation> process(String json, String personId) {
        JsonObject rootObject = JsonParser.parseString(json).getAsJsonObject();

        // Access the "sponsoredLegislation" array
        JsonArray sponsoredLegislationArray = rootObject.getAsJsonArray("sponsoredLegislation");

        List <SponsoredLegislation> sponsoredLegislations = new ArrayList<>();

        // Loop through each piece of sponsored legislation
        for (int i = 0; i < sponsoredLegislationArray.size(); i++) {
            JsonObject legislationObject = sponsoredLegislationArray.get(i).getAsJsonObject();

            Integer congressNo = legislationObject.has("congress") && !legislationObject.get("congress").isJsonNull() ?
            legislationObject.get("congress").getAsInt() : null;
            Integer billNo = legislationObject.has("number") && !legislationObject.get("number").isJsonNull() ?
                    legislationObject.get("number").getAsInt() : null;

            // Check if the legislation already exists in the database
            SponsoredLegislation legislationToProcess = sponsoredLegislationRepository.findByCongressAndBillNo(congressNo, billNo);

            Person existingMember = personRepository.findPersonByPersonId(personId);

            if (legislationToProcess == null) {
                legislationToProcess = new SponsoredLegislation();
                legislationToProcess.setCongress(congressNo);
                legislationToProcess.setBillNo(billNo);
            }

            legislationToProcess.setSponLegId(idGenerator.generateSponsLegId());

            legislationToProcess.setPerson(existingMember);

            // Set title
            String titleString = legislationObject.has("title") && !legislationObject.get("title").isJsonNull() ?
                    legislationObject.get("title").getAsString() : null;
            if (legislationToProcess.getLegTitle() == null || !legislationToProcess.getLegTitle().equals(titleString)) {
                legislationToProcess.setLegTitle(titleString);
            }

            // Set Type
            String billType = legislationObject.has("type") && !legislationObject.get("type").isJsonNull() ? legislationObject.get("type").getAsString() : null;
            legislationToProcess.setBillType(billType);

            // Set introduced date
            String introDtString = legislationObject.get("introducedDate").getAsString();
            LocalDate introducedDt = LocalDate.parse(introDtString);
            if (legislationToProcess.getIntroDt() == null || !legislationToProcess.getIntroDt().equals(introducedDt)) {
                legislationToProcess.setIntroDt(introducedDt);
            }

            // Process latest action
            JsonObject latestActionObject = legislationObject.getAsJsonObject("latestAction");
            if (latestActionObject != null) {
                String latestActionDate = latestActionObject.has("actionDate") && !latestActionObject.get("actionDate").isJsonNull() ? latestActionObject.get("actionDate").getAsString() : null;
                LocalDate actionDate = latestActionDate != null ? LocalDate.parse(latestActionDate) : null;
                String latestActionText = latestActionObject.has("text") && !latestActionObject.get("text").isJsonNull() ? latestActionObject.get("text").getAsString() : null;

                if (legislationToProcess.getLatestActionDt() == null || !legislationToProcess.getLatestActionDt().equals(actionDate)) {
                    legislationToProcess.setLatestActionDt(actionDate);
                }
                if (legislationToProcess.getLatestActionTxt() == null || !legislationToProcess.getLatestActionTxt().equals(latestActionText)) {
                    legislationToProcess.setLatestActionTxt(latestActionText);
                }
            }

            // Process policy area
            if(legislationObject.has("policyArea") && !legislationObject.get("policyArea").isJsonNull()) {
                JsonObject policyAreaObject = legislationObject.get("policyArea").getAsJsonObject();
                String policyArea = policyAreaObject.has("name") && !policyAreaObject.get("name").isJsonNull() ?
                        policyAreaObject.get("name").getAsString() : null;
                legislationToProcess.setPolicyArea(policyArea);
            }

            // Add url source
            String url = legislationObject.has("url") && !legislationObject.get("url").isJsonNull() ? legislationObject.get("url").getAsString() : null;
            legislationToProcess.setUrlSrc(url);

            sponsoredLegislations.add(legislationToProcess);
        }
        return sponsoredLegislations;
    }
}