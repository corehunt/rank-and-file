package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.Term;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.StateRepository;
import com.rankandfile.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Processor for handling Person entities based on JSON input.
 */
@Slf4j
@Component
public class PersonProcessor {

    private static final String FIELD_MEMBER = "member";
    private static final String FIELD_BIOGUIDE_ID = "bioguideId";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_MIDDLE_NAME = "middleName";
    private static final String FIELD_LAST_NAME = "lastName";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ADDRESS_INFORMATION = "addressInformation";
    private static final String FIELD_CITY = "city";
    private static final String FIELD_DISTRICT = "district";
    private static final String FIELD_ZIP_CODE = "zipCode";
    private static final String FIELD_OFFICE_ADDRESS = "officeAddress";
    private static final String FIELD_PHONE_NUMBER = "phoneNumber";
    private static final String FIELD_CURRENT_MEMBER = "currentMember";
    private static final String FIELD_OFFICIAL_WEBSITE_URL = "officialWebsiteUrl";
    private static final String FIELD_PARTY_HISTORY = "partyHistory";
    private static final String FIELD_PARTY_ABBREVIATION = "partyAbbreviation";
    private static final String FIELD_START_YEAR = "startYear";
    private static final String FIELD_DEPICTION = "depiction";
    private static final String FIELD_ATTRIBUTION = "attribution";
    private static final String FIELD_IMAGE_URL = "imageUrl";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_TERMS = "terms";
    private static final String FIELD_CHAMBER = "chamber";
    private static final String FIELD_CONGRESS = "congress";
    private static final String FIELD_END_YEAR = "endYear";
    private static final String FIELD_MEMBER_TYPE = "memberType";
    private static final String FIELD_STATE_CODE = "stateCode";
    private static final String FIELD_STATE_NAME = "stateName";

    private final PersonRepository personRepository;
    private final IdGenerator idGenerator;

    public PersonProcessor(PersonRepository personRepository, IdGenerator idGenerator) {
        this.personRepository = personRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Processes the JSON string containing a member and returns a populated Person entity.
     *
     * @param json The JSON string containing member data.
     * @return A populated Person entity or null if processing fails.
     */
    public Person validatePerson(String json) {
        log.info("Starting processing Person from JSON data");

        if (isNullOrEmpty(json)) {
            log.warn("Input JSON string is null or empty");
            return null;
        }

        JsonObject memberObject = parseMemberObject(json);
        if (memberObject == null) {
            return null;
        }

        String bioguideId = getAsString(memberObject, FIELD_BIOGUIDE_ID);
        if (bioguideId == null) {
            log.warn("Member missing '{}'. Skipping.", FIELD_BIOGUIDE_ID);
            return null;
        }

        // Fetch existing Person or create a new one
        Person person = personRepository.findById(bioguideId).orElseGet(() -> {
            Person newPerson = new Person();
            newPerson.setPersonId(bioguideId);
            log.info("Creating new Person with ID: {}", bioguideId);
            return newPerson;
        });

        // Update fields using helper methods
        updateNameFields(memberObject, person);
        updateAddressInformation(memberObject, person);
        updateCurrentMemberStatus(memberObject, person);
        updateWebsite(memberObject, person);
        updatePartyHistory(memberObject, person);
        updateDepiction(memberObject, person);
        updateState(memberObject, person);
        updateCurrentDistrict(memberObject, person);
        updateTerms(memberObject, person);

        log.info("Completed processing Person with ID: {}", person.getPersonId());
        return person;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Parses the member object from JSON.
     *
     * @param json The JSON string.
     * @return The member JsonObject or null if parsing fails.
     */
    private JsonObject parseMemberObject(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject member = root.getAsJsonObject(FIELD_MEMBER);
            if (member == null) {
                log.warn("JSON does not contain '{}' object", FIELD_MEMBER);
                return null;
            }
            return member;
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            return null;
        }
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsString() : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            try {
                return obj.get(field).getAsInt();
            } catch (NumberFormatException | UnsupportedOperationException e) {
                log.error("Invalid number format for field '{}': {}", field, obj.get(field).getAsString(), e);
            }
        }
        return null;
    }

    /**
     * Extracts name components from a full name string.
     * Expected format: "LastName, FirstName MiddleName"
     *
     * @param fullName The full name string.
     * @return An array containing [FirstName, MiddleName (optional), LastName]
     */
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

    /**
     * Cleans and formats the address components into a single address line.
     *
     * @param city     The city name.
     * @param district The district name.
     * @param zipCode  The ZIP code.
     * @return A formatted address string.
     */
    String cleanAddress(String city, String district, String zipCode) {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            sb.append(city);
            sb.append(" ");
        }
        if (district != null && !district.isEmpty()) {
            if (!sb.isEmpty()){
                sb.append(district);
            }
        }
        if (zipCode != null && !zipCode.isEmpty()) {
            if (!sb.isEmpty()){
                sb.append(", ");
                sb.append(zipCode);
            }

        }
        return sb.toString();
    }

    /**
     * Updates the name fields of the Person.
     *
     * @param memberObject The JSON object containing member data.
     * @param person       The Person entity to be updated.
     */
    private void updateNameFields(JsonObject memberObject, Person person) {
        String firstName = getAsString(memberObject, FIELD_FIRST_NAME);
        String middleName = getAsString(memberObject, FIELD_MIDDLE_NAME);
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

    /**
     * Updates the address information of the Person.
     *
     * @param memberObject The JSON object containing member data.
     * @param person       The Person entity to be updated.
     */
    private void updateAddressInformation(JsonObject memberObject, Person person) {
        if (memberObject.has(FIELD_ADDRESS_INFORMATION) && memberObject.get(FIELD_ADDRESS_INFORMATION).isJsonObject()) {
            JsonObject addressInfo = memberObject.getAsJsonObject(FIELD_ADDRESS_INFORMATION);
            String city = getAsString(addressInfo, FIELD_CITY);
            String district = getAsString(addressInfo, FIELD_DISTRICT);
            String zipCode = getAsString(addressInfo, FIELD_ZIP_CODE);
            String officeAddress = getAsString(addressInfo, FIELD_OFFICE_ADDRESS);
            String phoneNumber = getAsString(addressInfo, FIELD_PHONE_NUMBER);

            person.setOfficeLocLine1(officeAddress);
            person.setOfficeLocLine2(cleanAddress(city, district, zipCode));
            person.setPhoneNo(phoneNumber);
        }
    }

    private void  updateCurrentDistrict(JsonObject memberObject, Person person) {
        Integer currentDistrict = (memberObject.has(FIELD_DISTRICT) && !memberObject.get(FIELD_DISTRICT).isJsonNull())
                ? getAsInteger(memberObject, FIELD_DISTRICT) : null;
        if(currentDistrict != null) {
            person.setCurrentDistrict(currentDistrict);
        }
    }

    private void updateCurrentMemberStatus(JsonObject memberObject, Person person) {
        String currentMember = (memberObject.has(FIELD_CURRENT_MEMBER) && !memberObject.get(FIELD_CURRENT_MEMBER).isJsonNull())
                ? memberObject.get(FIELD_CURRENT_MEMBER).getAsString()
                : null;
        if (currentMember != null) {
            if (currentMember.equalsIgnoreCase("true")) {
                person.setCurrentMember("Yes");
            } else if (currentMember.equalsIgnoreCase("false")) {
                person.setCurrentMember("No");
            }
        } else {
            person.setCurrentMember(null);
        }
    }

    private void updateWebsite(JsonObject memberObject, Person person) {
        String website = (memberObject.has(FIELD_OFFICIAL_WEBSITE_URL) && !memberObject.get(FIELD_OFFICIAL_WEBSITE_URL).isJsonNull())
                ? memberObject.get(FIELD_OFFICIAL_WEBSITE_URL).getAsString()
                : null;
        person.setWebsite(website);
    }

    private void updatePartyHistory(JsonObject memberObject, Person person) {
        JsonArray partyHistoryArray = memberObject.getAsJsonArray(FIELD_PARTY_HISTORY);
        if (partyHistoryArray != null && !partyHistoryArray.isEmpty()) {
            JsonObject partyInfo = partyHistoryArray.get(0).getAsJsonObject();
            String partyMembership = (partyInfo.has(FIELD_PARTY_ABBREVIATION) && !partyInfo.get(FIELD_PARTY_ABBREVIATION).isJsonNull())
                    ? partyInfo.get(FIELD_PARTY_ABBREVIATION).getAsString()
                    : null;
            Integer partyStart = (partyInfo.has(FIELD_START_YEAR) && !partyInfo.get(FIELD_START_YEAR).isJsonNull())
                    ? partyInfo.get(FIELD_START_YEAR).getAsInt()
                    : null;
            person.setPartyMembership(partyMembership);
            person.setPartyStartYr(partyStart);
        }
    }

    private void updateDepiction(JsonObject memberObject, Person person) {
        if (memberObject.has(FIELD_DEPICTION) && memberObject.get(FIELD_DEPICTION).isJsonObject()) {
            JsonObject depiction = memberObject.getAsJsonObject(FIELD_DEPICTION);
            String attribution = (depiction.has(FIELD_ATTRIBUTION) && !depiction.get(FIELD_ATTRIBUTION).isJsonNull())
                    ? depiction.get(FIELD_ATTRIBUTION).getAsString()
                    : null;
            String imageUrl = (depiction.has(FIELD_IMAGE_URL) && !depiction.get(FIELD_IMAGE_URL).isJsonNull())
                    ? depiction.get(FIELD_IMAGE_URL).getAsString()
                    : null;
            person.setImgAttribution(attribution);
            person.setImageUrl(imageUrl);
        }
    }

    private void updateState(JsonObject memberObject, Person person) {
        String stateName = (memberObject.has(FIELD_STATE) && !memberObject.get(FIELD_STATE).isJsonNull())
                ? memberObject.get(FIELD_STATE).getAsString()
                : null;
        person.setState(stateName);
    }

    private void updateTerms(JsonObject memberObject, Person person) {
        JsonArray termsArray = memberObject.getAsJsonArray(FIELD_TERMS);
        if (termsArray == null || termsArray.isEmpty()) {
            log.debug("No terms found in JSON data for person ID: {}", person.getPersonId());
            return;
        }

        List<Term> existingTerms = person.getTermList();
        if (existingTerms == null) {
            existingTerms = new ArrayList<>();
            person.setTermList(existingTerms);
        }

        if (!existingTerms.isEmpty()) {
            // **Scenario 1: Existing Person with Existing Terms**

            // Create a map of existing terms for quick lookup using a unique key
            Map<String, Term> existingTermsMap = existingTerms.stream()
                    .collect(Collectors.toMap(this::generateTermKey, term -> term));

            // Set to keep track of processed terms
            Set<String> processedTermKeys = new HashSet<>();

            for (JsonElement termElement : termsArray) {
                if (!termElement.isJsonObject()) {
                    log.warn("Invalid term format, expected JsonObject but found: {}", termElement);
                    continue;
                }

                JsonObject termObject = termElement.getAsJsonObject();

                // Generate a unique key for the term based on chamber, congress, and startYear
                String termKey = generateTermKey(termObject);

                if (existingTermsMap.containsKey(termKey)) {
                    // **Update Existing Term**
                    Term existingTerm = existingTermsMap.get(termKey);
                    updateTermFields(termObject, existingTerm, person);
                    processedTermKeys.add(termKey);
                    log.debug("Updated existing term with key: {}", termKey);
                } else {
                    // **Add New Term**
                    Term newTerm = new Term();
                    newTerm.setTermId(idGenerator.generateTermId());
                    updateTermFields(termObject, newTerm, person);
                    existingTerms.add(newTerm);
                    processedTermKeys.add(termKey);
                    log.debug("Added new term with key: {}", termKey);
                }
            }

            // **Remove Orphaned Terms**
            existingTerms.removeIf(term -> {
                String key = generateTermKey(term);
                if (!processedTermKeys.contains(key)) {
                    log.debug("Removing term with key: {}", key);
                    return true;
                }
                return false;
            });

        } else {
            // **Scenario 2: New Person with No Existing Terms**

            for (JsonElement termElement : termsArray) {
                if (!termElement.isJsonObject()) {
                    log.warn("Invalid term format, expected JsonObject but found: {}", termElement);
                    continue;
                }

                JsonObject termObject = termElement.getAsJsonObject();

                // **Add All Terms as New Terms**
                Term newTerm = new Term();
                newTerm.setTermId(idGenerator.generateTermId());
                updateTermFields(termObject, newTerm, person);
                existingTerms.add(newTerm);
                log.debug("Added new term with generated termId: {}", newTerm.getTermId());
            }
        }
    }

    /**
     * Generates a unique key for a term based on chamber, congress, and startYear.
     *
     * @param term The Term entity.
     * @return A unique string key.
     */
    private String generateTermKey(Term term) {
        return String.format("%s-%d-%d", term.getChamber(), term.getCongress(), term.getStartYr());
    }

    /**
     * Generates a unique key for a term based on JSON term data.
     *
     * @param termObject The JSON object representing a term.
     * @return A unique string key.
     */
    private String generateTermKey(JsonObject termObject) {
        String chamber = getAsString(termObject, FIELD_CHAMBER);
        Integer congress = getAsInteger(termObject, FIELD_CONGRESS);
        Integer startYear = getAsInteger(termObject, FIELD_START_YEAR);
        return String.format("%s-%d-%d", chamber != null ? chamber : "UnknownChamber",
                congress != null ? congress : -1,
                startYear != null ? startYear : -1);
    }

    /**
     * Updates the fields of a Term entity based on the JSON term data.
     *
     * @param termObject The JSON object representing a term.
     * @param term       The Term entity to be updated.
     */
    private void updateTermFields(JsonObject termObject, Term term, Person person) {
        term.setChamber(getAsString(termObject, FIELD_CHAMBER));
        term.setCongress(getAsInteger(termObject, FIELD_CONGRESS));
        term.setDistrict(getAsInteger(termObject, FIELD_DISTRICT));
        term.setEndYr(getAsInteger(termObject, FIELD_END_YEAR));
        term.setMemberType(getAsString(termObject, FIELD_MEMBER_TYPE));
        term.setStartYr(getAsInteger(termObject, FIELD_START_YEAR));
        term.setStateCd(getAsString(termObject, FIELD_STATE_CODE));
        term.setStateNm(getAsString(termObject, FIELD_STATE_NAME));
        term.setPerson(person);
    }
}
