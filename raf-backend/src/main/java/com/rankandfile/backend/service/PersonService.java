package com.rankandfile.backend.service;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.config.ApiConfig;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.processor.PersonProcessor;
import com.rankandfile.backend.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class PersonService {

    private final WebClient webClient;
    private final Gson gson;
    private final PersonRepository personRepository;
    private final ApiConfig apiConfig;
    private final PersonProcessor personProcessor;

    public PersonService(WebClient.Builder webClientBuilder, PersonRepository personRepository, ApiConfig apiConfig, PersonProcessor personProcessor) {
        this.webClient = webClientBuilder.baseUrl("https://api.congress.gov/v3/").build();
        this.apiConfig = apiConfig;
        this.gson = new Gson();
        this.personRepository = personRepository;
        this.personProcessor = personProcessor;
    }

    public Person fetchAndProcessPerson(String bioguideId) {
        String response = this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/member/{bioguideId}")
                        .queryParam("api_key", apiConfig.getKey())
                        .build(bioguideId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return personProcessor.validatePerson(response);
    }

    private Person parsePersonFromJson(String json) {
        JsonObject memberObject = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("member");

        Person person = new Person();
//        person.setPersonId(memberObject.get("bioguideId").getAsString());
        person.setFirstName(memberObject.get("firstName").getAsString());
        person.setMidName(memberObject.has("middleName") ? memberObject.get("middleName").getAsString() : null);
        person.setLastName(memberObject.get("lastName").getAsString());
//        person.setBirthDate(memberObject.has("birthYear") ? LocalDate.parse(memberObject.get("birthYear").getAsString()) : null);
//        person.setDeathDate(memberObject.has("deathDate") ? LocalDate.parse(memberObject.get("deathDate").getAsString()) : null);
        person.setWebsite(memberObject.has("website") ? memberObject.get("website").getAsString() : null);
        person.setOfficeLocation(memberObject.has("officeLocation") ? memberObject.get("officeLocation").getAsString() : null);
        person.setPhoneNo(memberObject.has("phone") ? memberObject.get("phone").getAsString() : null);
//        person.setState(memberObject.get("state").getAsString());
        person.setDistrict(memberObject.has("district") ? memberObject.get("district").getAsString() : null);
        person.setBiography(memberObject.has("biography") ? memberObject.get("biography").getAsString() : null);
        person.setEmail(memberObject.has("email") ? memberObject.get("email").getAsString() : null);
        person.setImageUrl(memberObject.has("imageUrl") ? memberObject.get("imageUrl").getAsString() : null);
//        person.setPartyMembership(memberObject.get("partyName").getAsString());

        return person;
    }

    public void savePerson(Person person) {
        personRepository.save(person);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }


}
