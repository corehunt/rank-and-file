package com.rankandfile.backend.processor;

import com.google.gson.*;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CongressMemberProcessor {

    private static final String FIELD_MEMBERS = "members";
    private static final String FIELD_BIOGUIDE_ID = "bioguideId";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_MIDDLE_NAME = "middleName";
    private static final String FIELD_LAST_NAME = "lastName";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_DISTRICT = "district";
    private static final String FIELD_OFFICIAL_WEBSITE_URL = "officialWebsiteUrl";
    private static final String FIELD_PARTY_HISTORY = "partyHistory";
    private static final String FIELD_PARTY_ABBREVIATION = "partyAbbreviation";
    private static final String FIELD_START_YEAR = "startYear";
    private static final String FIELD_DEPICTION = "depiction";
    private static final String FIELD_ATTRIBUTION = "attribution";
    private static final String FIELD_IMAGE_URL = "imageUrl";
    private static final String FIELD_STATE = "state";

    private final PersonRepository personRepository;

    public CongressMemberProcessor(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Processes the JSON string containing members and returns a list of Person entities.
     *
     * @param json The JSON string containing members data.
     * @return A list of Person entities.
     */
    public List<Person> processMembers(String json) {
        log.info("Starting processing Congress members from JSON data");

        if (json == null || json.trim().isEmpty()) {
            log.warn("Input JSON string is null or empty");
            return Collections.emptyList();
        }

        JsonObject responseObject;
        try {
            responseObject = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return Collections.emptyList();
        }

        if (responseObject == null || !responseObject.has(FIELD_MEMBERS)) {
            log.warn("No members found in the provided JSON");
            return Collections.emptyList();
        }

        JsonArray membersArray = responseObject.getAsJsonArray(FIELD_MEMBERS);
        if (membersArray == null || membersArray.isEmpty()) {
            log.warn("Members array is empty");
            return Collections.emptyList();
        }

        List<Person> persons = new ArrayList<>();

        for (JsonElement element : membersArray) {
            JsonObject memberObject = element.getAsJsonObject();
            Person person = extractPersonFromJson(memberObject);
            if (person != null) {
                persons.add(person);
            }
        }

        log.info("Completed processing {} members", persons.size());
        return persons;
    }


    private Person extractPersonFromJson(JsonObject memberObject) {
        String bioguideId = getAsString(memberObject, FIELD_BIOGUIDE_ID);
        if (bioguideId == null) {
            log.warn("Member missing bioguideId. Skipping.");
            return null;
        }

        Person person = personRepository.findById(bioguideId).orElseGet(() -> {
            Person newPerson = new Person();
            newPerson.setPersonId(bioguideId);
            log.info("Creating new Person with ID: {}", bioguideId);
            return newPerson;
        });

        // Handle name fields
        handleNameFields(memberObject, person);

        // Current District
        Integer district = getAsInteger(memberObject, FIELD_DISTRICT);
        if (!Objects.equals(district, person.getCurrentDistrict())) {
            person.setCurrentDistrict(district);
        }

        // Website
        String website = getAsString(memberObject, FIELD_OFFICIAL_WEBSITE_URL);
        if (!Objects.equals(website, person.getWebsite())) {
            person.setWebsite(website);
        }

        // Party History
        handlePartyHistory(memberObject, person);

        // Depiction (Image Information)
        handleDepiction(memberObject, person);

        // State
        String state = getAsString(memberObject, FIELD_STATE);
        if (!Objects.equals(state, person.getState())) {
            person.setState(state);
        }

        return person;
    }

    private void handleNameFields(JsonObject memberObject, Person person) {
        String firstName = getAsString(memberObject, FIELD_FIRST_NAME);
        String middleName = getAsString(memberObject, FIELD_MIDDLE_NAME); // New line
        String lastName = getAsString(memberObject, FIELD_LAST_NAME);

        if (firstName != null && lastName != null) {
            person.setFirstName(firstName);
            person.setLastName(lastName);
            if (middleName != null) {
                person.setMidName(middleName);
                person.setFullName(firstName + " " + middleName + " " + lastName);
            } else {
                person.setFullName(firstName + " " + lastName);
            }
        } else {
            // Existing logic for handling 'name' field
            String name = getAsString(memberObject, FIELD_NAME);
            if (name != null) {
                String[] nameArray = extractNames(name);
                if (nameArray[0] != null && nameArray[1] != null) {
                    person.setFirstName(nameArray[0]);
                    person.setLastName(nameArray[nameArray.length - 1]);
                    person.setFullName(String.join(" ", nameArray));
                    if (nameArray.length == 3) {
                        person.setMidName(nameArray[1]);
                    }
                } else {
                    log.warn("Unable to extract names for member with ID {} due to invalid name format.", person.getPersonId());
                }
            } else {
                log.warn("Member with ID {} missing name information.", person.getPersonId());
            }
        }
    }

    private void handlePartyHistory(JsonObject memberObject, Person person) {
        if (memberObject.has(FIELD_PARTY_HISTORY) && !memberObject.get(FIELD_PARTY_HISTORY).isJsonNull()) {
            JsonArray partyHistoryArray = memberObject.getAsJsonArray(FIELD_PARTY_HISTORY);
            if (partyHistoryArray != null && !partyHistoryArray.isEmpty()) {
                JsonObject partyInfo = partyHistoryArray.get(0).getAsJsonObject();
                String partyAbbreviation = getAsString(partyInfo, FIELD_PARTY_ABBREVIATION);
                Integer startYear = getAsInteger(partyInfo, FIELD_START_YEAR);

                if (!Objects.equals(partyAbbreviation, person.getPartyMembership())) {
                    person.setPartyMembership(partyAbbreviation);
                }
                if (!Objects.equals(startYear, person.getPartyStartYr())) {
                    person.setPartyStartYr(startYear);
                }
            }
        }
    }

    private void handleDepiction(JsonObject memberObject, Person person) {
        if (memberObject.has(FIELD_DEPICTION) && !memberObject.get(FIELD_DEPICTION).isJsonNull()) {
            JsonObject depictionObject = memberObject.getAsJsonObject(FIELD_DEPICTION);
            String attribution = getAsString(depictionObject, FIELD_ATTRIBUTION);
            String imageUrl = getAsString(depictionObject, FIELD_IMAGE_URL);

            if (!Objects.equals(attribution, person.getImgAttribution())) {
                person.setImgAttribution(attribution);
            }
            if (!Objects.equals(imageUrl, person.getImageUrl())) {
                person.setImageUrl(imageUrl);
            }
        }
    }

    private String[] extractNames(String fullName) {
        // Handle names in the format "LastName, FirstName MiddleName"
        if (fullName == null || !fullName.contains(",")) {
            log.warn("Invalid name format: {}", fullName);
            return new String[]{null, null};
        }
        String[] nameParts = fullName.split(",\\s*");
        if (nameParts.length != 2) {
            log.warn("Invalid name format: {}", fullName);
            return new String[]{null, null};
        }
        String lastName = nameParts[0].trim();
        String[] firstNameParts = nameParts[1].trim().split("\\s+");
        String firstName = firstNameParts[0];
        String middleName = "";
        if (firstNameParts.length > 1) {
            middleName = String.join(" ", Arrays.copyOfRange(firstNameParts, 1, firstNameParts.length));
        }
        if (middleName.isEmpty()) {
            return new String[]{firstName, lastName};
        } else {
            return new String[]{firstName, middleName, lastName};
        }
    }



    private String getAsString(JsonObject obj, String field) {
        return obj.has(field) && !obj.get(field).isJsonNull() ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            try {
                return obj.get(field).getAsInt();
            } catch (NumberFormatException e) {
                log.error("Invalid number format for field '{}': {}", field, obj.get(field).getAsString(), e);
            }
        }
        return null;
    }
}
