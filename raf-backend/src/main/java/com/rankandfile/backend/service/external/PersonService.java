package com.rankandfile.backend.service.external;

import com.rankandfile.backend.config.ApiConfig;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.processor.CongressMemberProcessor;
import com.rankandfile.backend.processor.PersonProcessor;
import com.rankandfile.backend.processor.SponsoredLegislationProcessor;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.repository.SponsoredLegislationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PersonService {

    private final WebClient webClient;
    private final PersonRepository personRepository;
    private final ApiConfig apiConfig;
    private final PersonProcessor personProcessor;
    private final CongressMemberProcessor congressMemberProcessor;
    private final SponsoredLegislationProcessor sponsoredLegislationProcessor;
    private final SponsoredLegislationRepository sponsoredLegislationRepository;

    public PersonService(WebClient.Builder webClientBuilder, PersonRepository personRepository, ApiConfig apiConfig, PersonProcessor personProcessor, CongressMemberProcessor congressMemberProcessor, SponsoredLegislationProcessor sponsoredLegislationProcessor, SponsoredLegislationRepository sponsoredLegislationRepository) {
        this.congressMemberProcessor = congressMemberProcessor;
        this.sponsoredLegislationProcessor = sponsoredLegislationProcessor;
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
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

        personRepository.saveAll(allMembers);

        return allMembers;
    }

    public List<SponsoredLegislation> getSponsoredLegislationByPersonId(String personId) {
        List<SponsoredLegislation> allSponsLegList = new ArrayList<>();
        int limit = 250;
        int offset = 0;
        boolean hasMoreRecords = true;

        while (hasMoreRecords) {
            final int currentOffset = offset;

            log.info("Fetching sponsored legislation for personId: {}, offset: {}", personId, currentOffset);
            try {
                String response = this.webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("member/{personId}/sponsored-legislation")
                                .queryParam("api_key", apiConfig.getKey())
                                .queryParam("limit", limit)
                                .queryParam("offset", currentOffset)
                                .build(personId))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (response == null || response.isEmpty()) {
                    log.warn("Received empty response from API for personId: {}", personId);
                    break;
                }

                List<SponsoredLegislation> legislationList = sponsoredLegislationProcessor.process(response, personId);
                allSponsLegList.addAll(legislationList);

                if (legislationList.size() < limit) {
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                }
            } catch (Exception e) {
                log.error("An error occurred while processing cosponsored legislation for personId: {}", personId, e);
                hasMoreRecords = false;
            }
        }

        sponsoredLegislationRepository.saveAll(allSponsLegList);

        return allSponsLegList;
    }

    public List<SponsoredLegislation> getCoSponsoredLegislationByPersonId(String personId) {
        List<SponsoredLegislation> allSponsLegList = new ArrayList<>();
        int limit = 250;
        int offset = 0;
        boolean hasMoreRecords = true;

        while (hasMoreRecords) {
            final int currentOffset = offset;

            log.info("Fetching cosponsored legislation for personId: {}, offset: {}", personId, currentOffset);
            try {
                String response = this.webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("member/{personId}/cosponsored-legislation")
                                .queryParam("api_key", apiConfig.getKey())
                                .queryParam("limit", limit)
                                .queryParam("offset", currentOffset) // Use 'currentOffset' here
                                .build(personId))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (response == null || response.isEmpty()) {
                    log.warn("Received empty response from API for personId: {}", personId);
                    break;
                }

                List<SponsoredLegislation> legislationList = sponsoredLegislationProcessor.process(response, personId);
                allSponsLegList.addAll(legislationList);

                if (legislationList.size() < limit) {
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                }
            } catch (Exception e) {
                log.error("An error occurred while processing cosponsored legislation for personId: {}", personId, e);
                hasMoreRecords = false;
            }
        }

        sponsoredLegislationRepository.saveAll(allSponsLegList);

        return allSponsLegList;
    }

    public void savePerson(Person person) {
        personRepository.save(person);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public List<Person> getPersonListByFullName(String searchTerm) {
        return personRepository.findPersonByFullNameSearchTerm(searchTerm);
    }

    public Person getPersonById(String personId) {
        return personRepository.findPersonByPersonId(personId);
    }


}
