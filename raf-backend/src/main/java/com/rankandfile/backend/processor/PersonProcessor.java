package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.Term;
import com.rankandfile.backend.entity.domain.StateDomain;
import com.rankandfile.backend.repository.StateRepository;
import com.rankandfile.backend.util.IdGenerator;
import com.rankandfile.backend.util.Supplier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

@Component
public class PersonProcessor {

    private final StateRepository stateRepository;
    private final IdGenerator idGenerator;

    private Supplier personSupplier;

    public PersonProcessor(StateRepository stateRepository, Supplier personSupplier, IdGenerator idGenerator) {
        this.stateRepository = stateRepository;
        this.personSupplier = personSupplier;
        this.idGenerator = idGenerator;
    }

    public Person validatePerson(String json) {
        JsonObject memberObject = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("member");

        Person person = personSupplier.findOrCreatePerson(memberObject.get("bioguideId").getAsString());

        person.setPersonId(memberObject.get("bioguideId").getAsString());

        if (memberObject.has("firstName") && !memberObject.get("firstName").isJsonNull() && memberObject.has("lastName") && !memberObject.get("lastName").isJsonNull()) {
            String firstName = memberObject.get("firstName").getAsString();
            String lastName = memberObject.get("lastName").getAsString();
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setFullName(firstName + " " + lastName);
        } else if (memberObject.has("name") && !memberObject.get("name").isJsonNull()) {
            String[] nameArray = extractNames(memberObject.get("name").getAsString());
            String firstName = nameArray[0];
            person.setFirstName(firstName);
            if (nameArray.length == 2) {
                String lastName = nameArray[1];
                person.setLastName(lastName);
                person.setFullName(firstName + " " + lastName);
            } else if (nameArray.length == 3) {
                String midName = nameArray[1];
                String lastName = nameArray[2];
                person.setMidName(midName);
                person.setLastName(lastName);
                person.setFullName(firstName + " " + midName + " " + lastName);
            }
        } else {
            person.setFirstName(null);
            person.setLastName(null);
        }

        if (memberObject.has("addressInformation") && memberObject.get("addressInformation").isJsonObject()) {
            JsonObject addressInformationObject = memberObject.getAsJsonObject("addressInformation");
            String city = addressInformationObject.has("city") && !addressInformationObject.get("city").isJsonNull() ? addressInformationObject.get("city").getAsString() : null;
            String district = addressInformationObject.has("district") && !addressInformationObject.get("district").isJsonNull() ? addressInformationObject.get("district").getAsString() : null;
            String zipCode = addressInformationObject.has("zipCode") && !addressInformationObject.get("zipCode").isJsonNull() ? addressInformationObject.get("zipCode").getAsString() : null;
            String officeAddress = addressInformationObject.has("officeAddress") && !addressInformationObject.get("officeAddress").isJsonNull() ? addressInformationObject.get("officeAddress").getAsString() : null;
            String phoneNumber = addressInformationObject.has("phoneNumber") && !addressInformationObject.get("phoneNumber").isJsonNull() ? addressInformationObject.get("phoneNumber").getAsString() : null;

            person.setOfficeLocLine1(officeAddress);
            person.setOfficeLocLine2(cleanAddress(city, district, zipCode));
            person.setPhoneNo(phoneNumber);
            person.setState(district);
        }

        person.setCurrentDistrict(memberObject.has("district") && !memberObject.get("district").isJsonNull() ? memberObject.get("district").getAsInt() : null);

        String currentMember = (memberObject.has("currentMember") && !memberObject.get("currentMember").isJsonNull() ? memberObject.get("currentMember").getAsString() : null);
        if(currentMember.equals("true")) {
            person.setCurrentMember("Yes");
        } else if (currentMember.equals("false")) {
            person.setCurrentMember("No");
        } else {
            person.setCurrentMember(null);
        }

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

        person.setState(memberObject.has("state") && !memberObject.get("state").isJsonNull() ? memberObject.get("state").getAsString() : null);

        JsonArray termsArray = memberObject.getAsJsonArray("terms");
        if (termsArray != null && !termsArray.isEmpty()) {
            List<Term> terms = new ArrayList<>();
            for (int i = 0; i < termsArray.size(); i++) {
                JsonObject termObject = termsArray.get(i).getAsJsonObject();
                Term term = new Term();
                term.setTermId(idGenerator.generateTermId());
                term.setPerson(person);
                term.setChamber(termObject.has("chamber") && !termObject.get("chamber").isJsonNull() ? termObject.get("chamber").getAsString() : null);
                term.setCongress(termObject.has("congress") && !termObject.get("congress").isJsonNull() ? termObject.get("congress").getAsInt() : null);
                term.setDistrict(termObject.has("district") && !termObject.get("district").isJsonNull() ? termObject.get("district").getAsInt() : null);
                term.setEndYr(termObject.has("endYear") && !termObject.get("endYear").isJsonNull() ? termObject.get("endYear").getAsInt() : null);
                term.setMemberType(termObject.has("memberType") && !termObject.get("memberType").isJsonNull() ? termObject.get("memberType").getAsString() : null);
                term.setStartYr(termObject.has("startYear") && !termObject.get("startYear").isJsonNull() ? termObject.get("startYear").getAsInt() : null);
                term.setStateCd(termObject.has("stateCode") && !termObject.get("stateCode").isJsonNull() ? termObject.get("stateCode").getAsString() : null);
                term.setStateNm(termObject.has("stateName") && !termObject.get("stateName").isJsonNull() ? termObject.get("stateName").getAsString() : null);
                terms.add(term);
            }
            person.setTermList(terms);
        }

        return person;

    }

    public String cleanAddress(String addressStr1, String addressStr2, String addressStr3) {
        Formatter formatter = new Formatter();
        formatter.format("%s, %s %s", addressStr1, addressStr2, addressStr3);
        String finalAddress = formatter.toString();
        formatter.close();

        return finalAddress;
    }

    public String getStateAbbrByFullName(String stateName) {
        if(stateName != null) {
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
