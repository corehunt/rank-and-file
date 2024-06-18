package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.domain.StateDomain;
import com.rankandfile.backend.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CongressMemberProcessor {

    @Autowired
    private final StateRepository stateRepository;

    public CongressMemberProcessor(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }


    public List<Person> processMembers(String json) {
        JsonObject responseObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray membersArray = responseObject.getAsJsonArray("members");

        List<Person> persons = new ArrayList<>();

        for (int i = 0; i < membersArray.size(); i++) {
            JsonObject memberObject = membersArray.get(i).getAsJsonObject();
            Person person = extractPersonFromJson(memberObject);
            persons.add(person);
        }

        return persons;
    }

    private Person extractPersonFromJson(JsonObject memberObject) {
        Person person = new Person();

        person.setPersonId(memberObject.has("bioguideId") && !memberObject.get("bioguideId").isJsonNull() ? memberObject.get("bioguideId").getAsString() : null);

        // Handle name fields
        if (memberObject.has("firstName") && !memberObject.get("firstName").isJsonNull() && memberObject.has("lastName") && !memberObject.get("lastName").isJsonNull()) {
            person.setFirstName(memberObject.get("firstName").getAsString());
            person.setLastName(memberObject.get("lastName").getAsString());
        } else if (memberObject.has("name") && !memberObject.get("name").isJsonNull()) {
            String[] nameArray = extractNames(memberObject.get("name").getAsString());
            person.setFirstName(nameArray[0]);
            if (nameArray.length == 2) {
                person.setLastName(nameArray[1]);
            } else if (nameArray.length == 3) {
                person.setMidName(nameArray[1]);
                person.setLastName(nameArray[2]);
            }
        } else {
            person.setFirstName(null);
            person.setLastName(null);
        }

        person.setCurrentDistrict(memberObject.has("district") && !memberObject.get("district").isJsonNull() ? memberObject.get("district").getAsInt() : null);

        person.setWebsite(memberObject.has("officialWebsiteUrl") && !memberObject.get("officialWebsiteUrl").isJsonNull() ? memberObject.get("officialWebsiteUrl").getAsString() : null);

        JsonArray partyHistoryArray = memberObject.getAsJsonArray("partyHistory");
        if (partyHistoryArray != null && !partyHistoryArray.isEmpty()) {
            JsonObject partyInformationObject = partyHistoryArray.get(0).getAsJsonObject();
            String partyMembership = partyInformationObject.has("partyAbbreviation") && !partyInformationObject.get("partyAbbreviation").isJsonNull() ? partyInformationObject.get("partyAbbreviation").getAsString() : null;
            Integer partyStart = partyInformationObject.has("startYear") && !partyInformationObject.get("startYear").isJsonNull() ? partyInformationObject.get("startYear").getAsInt() : null;
            person.setPartyMembership(partyMembership);
            person.setPartyStartYr(partyStart);
        }

        if (memberObject.has("depiction") && memberObject.get("depiction").isJsonObject()) {
            JsonObject imageInformation = memberObject.getAsJsonObject("depiction");
            String attribution = imageInformation.has("attribution") && !imageInformation.get("attribution").isJsonNull() ? imageInformation.get("attribution").getAsString() : null;
            String imgUrl = imageInformation.has("imageUrl") && !imageInformation.get("imageUrl").isJsonNull() ? imageInformation.get("imageUrl").getAsString() : null;

            person.setImgAttribution(attribution);
            person.setImageUrl(imgUrl);
        }

        String stateString = getStateAbbrByFullName(memberObject.has("state") && !memberObject.get("state").isJsonNull() ? memberObject.get("state").getAsString() : null);
        person.setState(stateString);

        return person;
    }

    private String getStateAbbrByFullName(String stateName) {
        if (stateName != null) {
            Optional<StateDomain> state = stateRepository.findByStateNm(stateName);
            return state.map(StateDomain::getStateAbbr).orElse(null); // Return state abbreviation if found, else null
        } else {
            return null;
        }
    }

    private String[] extractNames(String fullName) {
        // Split the full name into last name and first name parts
        String[] nameParts = fullName.split(",\\s*");
        String lastName = nameParts[0].trim();
        String firstName = "";
        String middleName = "";

        if (nameParts.length > 1) {
            String[] firstNameParts = nameParts[1].trim().split("\\s+");
            firstName = firstNameParts[0];
            if (firstNameParts.length > 1) {
                middleName = firstNameParts[1];
            }
        }

        if (middleName.isEmpty()) {
            return new String[]{firstName, lastName};
        } else {
            return new String[]{firstName, middleName, lastName};
        }
    }
}