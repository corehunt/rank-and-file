package com.rankandfile.backend.service;

import com.google.gson.Gson;

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

    public void savePerson(Person person) {
        personRepository.save(person);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }


}
