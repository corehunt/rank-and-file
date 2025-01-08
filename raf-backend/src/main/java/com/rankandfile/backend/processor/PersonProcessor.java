package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Leadership;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.Term;
import com.rankandfile.backend.entity.domain.StateDomain;
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
    private static final String FIELD_PARTY = "partyName";
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
    private static final String FIELD_LEADERSHIP = "leadership";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_CURRENT = "current";

    private final PersonRepository personRepository;
    private final StateRepository stateRepository;
    private final IdGenerator idGenerator;

    public PersonProcessor(PersonRepository personRepository, StateRepository stateRepository, IdGenerator idGenerator) {
        this.personRepository = personRepository;
        this.stateRepository = stateRepository;
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
        //updateNameFields(memberObject, person); --> commenting out here as format from all members endpoint is cleaner
        updateAddressInformation(memberObject, person);
        updateCurrentMemberStatus(memberObject, person);
        updateWebsite(memberObject, person);
        updatePartyHistory(memberObject, person);
        updateDepiction(memberObject, person);
        updateState(memberObject, person);
        updateCurrentDistrict(memberObject, person);
        updateTerms(memberObject, person);
        updateLeaderships(memberObject, person);

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
                // Check if the name is in "LastName, FirstName MiddleName(s), Suffix" format
                if (name.contains(",")) {
                    parseCommaSeparatedName(name, person);
                } else {
                    // Handle format: "FirstName MiddleName LastName"
                    parseSpaceSeparatedName(name, person);
                }
            } else {
                log.warn("Member with ID {} missing name information.", person.getPersonId());
            }
        }
    }

    private void parseCommaSeparatedName(String name, Person person) {
        String[] parts = name.split(",");
        if (parts.length >= 2) {
            String lastNamePart = parts[0].trim();
            String firstNamePart = parts[1].trim();
            String suffix = null;
            if (parts.length > 2) {
                suffix = parts[2].trim();
            }

            // Split firstNamePart into first name and middle name(s)
            String[] nameParts = firstNamePart.split("\\s+");
            if (nameParts.length >= 1) {
                person.setFirstName(nameParts[0]);
                if (nameParts.length > 1) {
                    String middleName = String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length));
                    person.setMidName(middleName);
                }
                person.setLastName(lastNamePart);

                // Construct full name
                StringBuilder fullNameBuilder = new StringBuilder();
                fullNameBuilder.append(person.getFirstName());
                if (person.getMidName() != null) {
                    fullNameBuilder.append(" ").append(person.getMidName());
                }
                fullNameBuilder.append(" ").append(person.getLastName());
                if (suffix != null && !suffix.isEmpty()) {
                    fullNameBuilder.append(", ").append(suffix);
                }
                person.setFullName(fullNameBuilder.toString());
            } else {
                log.warn("Unable to extract names for member with ID {} due to invalid first name format.", person.getPersonId());
            }
        } else {
            log.warn("Unable to extract names for member with ID {} due to invalid comma-separated name format.", person.getPersonId());
        }
    }

    private void parseSpaceSeparatedName(String name, Person person) {
        String[] nameArray = name.trim().split("\\s+");
        if (nameArray.length >= 2) {
            person.setFirstName(nameArray[0]);
            person.setLastName(nameArray[nameArray.length - 1]);
            if (nameArray.length > 2) {
                String middleName = String.join(" ", Arrays.copyOfRange(nameArray, 1, nameArray.length - 1));
                person.setMidName(middleName);
            }
            person.setFullName(name);
        } else {
            log.warn("Unable to extract names for member with ID {} due to invalid space-separated name format.", person.getPersonId());
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

            String party = (partyInfo.has(FIELD_PARTY) && !partyInfo.get(FIELD_PARTY).isJsonNull())
                    ? partyInfo.get(FIELD_PARTY).getAsString()
                    : null;

            Integer partyStart = (partyInfo.has(FIELD_START_YEAR) && !partyInfo.get(FIELD_START_YEAR).isJsonNull())
                    ? partyInfo.get(FIELD_START_YEAR).getAsInt()
                    : null;

            person.setPartyMembership(partyMembership);
            person.setParty(party);
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

            Optional<StateDomain> stateDom = stateRepository.findByStateNm(stateName);
            if (stateDom.isPresent()) {
                String stateAbbr = stateDom.get().getStateAbbr();
                person.setStateAbbr(stateAbbr);
            }

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

    private void updateLeaderships(JsonObject memberObject, Person person) {
        JsonArray leadershipArray = memberObject.getAsJsonArray("leadership");
        if (leadershipArray == null || leadershipArray.isEmpty()) {
            log.debug("No leadership entries found in JSON data for person ID: {}", person.getPersonId());
            return;
        }

        List<Leadership> existingLeaderships = person.getLeadershipList();
        if (existingLeaderships == null) {
            existingLeaderships = new ArrayList<>();
            person.setLeadershipList(existingLeaderships);
        }

        // Create a map of existing leaderships for quick lookup using a unique key
        Map<String, Leadership> existingLeadershipMap = existingLeaderships.stream()
                .collect(Collectors.toMap(
                        this::generateLeadershipKey,
                        l -> l,
                        (existing, duplicate) -> {
                            // Log a warning about the duplicate
                            log.warn("Duplicate leadership found for key: {}. Keeping leadershipId: {} and ignoring leadershipId: {}",
                                    this.generateLeadershipKey(existing), existing.getLeadershipId(), duplicate.getLeadershipId());
                            // Decide which Leadership to keep. Here, we keep the existing one.
                            return existing;
                        }
                ));

        // Set to keep track of processed leadership keys
        Set<String> processedLeadershipKeys = new HashSet<>();

        for (JsonElement leadershipElement : leadershipArray) {
            if (!leadershipElement.isJsonObject()) {
                log.warn("Invalid leadership format, expected JsonObject but found: {}", leadershipElement);
                continue;
            }

            JsonObject leadershipObject = leadershipElement.getAsJsonObject();

            // Generate a unique key for the leadership based on 'congress' and 'type'
            String leadershipKey = generateLeadershipKey(leadershipObject);

            // Extract congress number from the leadership object
            String congressNumber = leadershipObject.has("congress") ? leadershipObject.get("congress").getAsString() : null;
            if (congressNumber == null) {
                log.warn("Leadership entry missing 'congress' field: {}", leadershipObject);
                continue;
            }

            // Determine if this leadership is current
            boolean isCurrent = leadershipObject.has("current") && leadershipObject.get("current").getAsBoolean();

            if (existingLeadershipMap.containsKey(leadershipKey)) {
                // **Update Existing Leadership**
                Leadership existingLeadership = existingLeadershipMap.get(leadershipKey);
                updateLeadershipFields(leadershipObject, existingLeadership, person);
                existingLeadership.setCurrentLeader(isCurrent ? "true" : "false");
                processedLeadershipKeys.add(leadershipKey);
                log.debug("Updated existing leadership with key: {}, current: {}", leadershipKey, isCurrent);
            } else {
                // **Add New Leadership**
                Leadership newLeadership = new Leadership();
                newLeadership.setLeadershipId(idGenerator.generateLeadershipId());
                newLeadership.setPerson(person);
                updateLeadershipFields(leadershipObject, newLeadership, person);
                newLeadership.setCongress(congressNumber);
                newLeadership.setCurrentLeader(isCurrent ? "true" : "false");
                existingLeaderships.add(newLeadership);
                processedLeadershipKeys.add(leadershipKey);
                log.debug("Added new leadership with key: {}, current: {}", leadershipKey, isCurrent);
            }
        }

        // **Update 'currentLeader' flags for existing leaderships not present in the latest data**
        for (Leadership existingLeadership : existingLeaderships) {
            String key = generateLeadershipKey(existingLeadership);
            if (!processedLeadershipKeys.contains(key)) {
                // If the existing leadership was previously marked as current, mark it as not current
                if ("true".equals(existingLeadership.getCurrentLeader())) {
                    existingLeadership.setCurrentLeader("false");
                    log.debug("Marked leadership as not current with key: {}", key);
                }
                // Historical leaderships remain unchanged if they were already not current
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

    /**
     * Generates a unique key for a Leadership object from its key fields.
     * For example, you could combine congress and leadershipType.
     */
    private String generateLeadershipKey(Leadership leadership) {
        // Adjust as needed based on what makes a leadership unique
        String congress = leadership.getCongress() != null ? leadership.getCongress() : "";
        String leadershipType = leadership.getLeadershipType() != null ? leadership.getLeadershipType() : "";
        return congress + "_" + leadershipType;
    }

    /**
     * Generates a unique key for a Leadership from a JSON object
     */
    private String generateLeadershipKey(JsonObject leadershipObject) {
        String congress = leadershipObject.has(FIELD_CONGRESS) ? leadershipObject.get(FIELD_CONGRESS).getAsString() : "";
        String leadershipType = leadershipObject.has(FIELD_TYPE) ? leadershipObject.get(FIELD_TYPE).getAsString() : "";
        return congress + "_" + leadershipType;
    }

    /**
     * Updates Leadership fields from the JsonObject.
     * Similar to updateTermFields, but for leadership.
     */
    private void updateLeadershipFields(JsonObject leadershipObject, Leadership leadership, Person person) {
        leadership.setPerson(person);
        if (leadershipObject.has(FIELD_CONGRESS)) {
            leadership.setCongress(leadershipObject.get(FIELD_CONGRESS).getAsString());
        }
        if (leadershipObject.has(FIELD_TYPE)) {
            leadership.setLeadershipType(leadershipObject.get(FIELD_TYPE).getAsString());
        }
        if (leadershipObject.has(FIELD_CURRENT)) {
            leadership.setCurrentLeader(leadershipObject.get(FIELD_CURRENT).getAsString());
        }
    }

}
