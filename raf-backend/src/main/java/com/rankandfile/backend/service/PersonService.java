package com.rankandfile.backend.service;

import com.rankandfile.backend.config.ApiConfig;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.processor.CongressMemberProcessor;
import com.rankandfile.backend.processor.PersonProcessor;
import com.rankandfile.backend.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {

    private final WebClient webClient;
    private final PersonRepository personRepository;
    private final ApiConfig apiConfig;
    private final PersonProcessor personProcessor;
    private final CongressMemberProcessor congressMemberProcessor;

    public PersonService(WebClient.Builder webClientBuilder, PersonRepository personRepository, ApiConfig apiConfig, PersonProcessor personProcessor, CongressMemberProcessor congressMemberProcessor) {
        this.congressMemberProcessor = congressMemberProcessor;
        this.webClient = webClientBuilder.baseUrl(apiConfig.getUrl()).build();
        this.apiConfig = apiConfig;
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

    public List<Person> fetchMembersOfCurrentCongress(String congressNo) {
        List<Person> allMembers = new ArrayList<>();
        int limit = 250;
        int offset = 0;
        boolean hasMoreRecords = true;

        while (hasMoreRecords) {
            int finalOffset = offset;
            String response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("member/congress/{congress}")
                            .queryParam("api_key", apiConfig.getKey())
                            .queryParam("limit", limit)
                            .queryParam("offset", finalOffset)
                            .build(congressNo))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<Person> persons = congressMemberProcessor.processMembers(response);
            allMembers.addAll(persons);
            offset += limit;

            // If the number of persons fetched is less than the limit, we've reached the end
            if (persons.size() < limit) {
                hasMoreRecords = false;
            }
        }

        for (Person person : allMembers) {
            personRepository.save(person);
        }

        return allMembers;
    }

    public void savePerson(Person person) {
        personRepository.save(person);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }


}
