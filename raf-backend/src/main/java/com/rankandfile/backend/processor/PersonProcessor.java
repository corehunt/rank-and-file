package com.rankandfile.backend.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.domain.StateDomain;
import com.rankandfile.backend.repository.StateRepository;
import com.rankandfile.backend.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Formatter;
import java.util.Optional;

@Component
public class PersonProcessor {

    private final IdGenerator idGenerator;

    @Autowired
    private final StateRepository stateRepository;

    public PersonProcessor(IdGenerator idGenerator,
                           StateRepository stateRepository) {
        this.idGenerator = idGenerator;
        this.stateRepository = stateRepository;
    }

    public Person validatePerson(String json) {
        JsonObject memberObject = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("member");

        Person person = new Person();
        person.setPersonId(memberObject.get("bioguideId").getAsString());

        person.setFirstName(memberObject.has("firstName") ? memberObject.get("firstName").getAsString() : null);
        person.setLastName(memberObject.has("lastName") ? memberObject.get("lastName").getAsString() : null);

        if (memberObject.has("addressInformation") && memberObject.get("addressInformation").isJsonObject()) {
            JsonObject addressInformationObject = memberObject.getAsJsonObject("addressInformation");
            String city = addressInformationObject.has("city") ? addressInformationObject.get("city").getAsString() : null;
            String district = addressInformationObject.has("district") ? addressInformationObject.get("district").getAsString() : null;
            String zipCode = addressInformationObject.has("zipCode") ? addressInformationObject.get("zipCode").getAsString() : null;
            String officeAddress = addressInformationObject.has("officeAddress") ? addressInformationObject.get("officeAddress").getAsString() : null;
            String phoneNumber = addressInformationObject.has("phoneNumber") ? addressInformationObject.get("phoneNumber").getAsString() : null;

            person.setOfficeLocLine1(officeAddress);
            person.setOfficeLocLine2(cleanAddress(city, district, zipCode));
            person.setPhoneNo(phoneNumber);
            person.setPhoneNo(phoneNumber);
            person.setState(district);
        }

        person.setCurrentDistrict(memberObject.has("district") ? memberObject.get("district").getAsInt() : null);

        person.setWebsite(memberObject.has("officialWebsiteUrl") ? memberObject.get("officialWebsiteUrl").getAsString() : null);

        JsonArray partyHistoryArray = memberObject.getAsJsonArray("partyHistory");
        if (!partyHistoryArray.isEmpty()) {
            JsonObject partyInformationObject = partyHistoryArray.get(0).getAsJsonObject();
            String partyMembership = partyInformationObject.get("partyAbbreviation").getAsString();
            person.setPartyMembership(partyMembership);
            Integer partyStart = partyInformationObject.get("startYear").getAsInt();
            person.setPartyStartYr(partyStart);
        }

        if (memberObject.has("depiction") && memberObject.get("depiction").isJsonObject()) {
            JsonObject imageInformation = memberObject.getAsJsonObject("depiction");
            String attribution = imageInformation.has("attribution") ? imageInformation.get("attribution").getAsString() : null;
            String imgUrl = imageInformation.has("imageUrl") ? imageInformation.get("imageUrl").getAsString() : null;

            person.setImgAttribution(attribution);
            person.setImageUrl(imgUrl);
        }

        String stateString = getStateAbbrByFullName(memberObject.get("state").getAsString());
        person.setState(stateString);

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

}
