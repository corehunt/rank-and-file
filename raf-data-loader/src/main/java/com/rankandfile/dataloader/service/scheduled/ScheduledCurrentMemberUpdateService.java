package com.rankandfile.dataloader.service.scheduled;

import com.rankandfile.dataloader.config.ApiConfig;
import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.processor.PersonProcessor;
import com.rankandfile.dataloader.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class ScheduledCurrentMemberUpdateService {

    private final WebClient webClient;
    private final PersonProcessor personProcessor;
    private final PersonRepository personRepository;
    private final ApiConfig apiConfig;

    @Autowired
    public ScheduledCurrentMemberUpdateService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            PersonProcessor personProcessor,
            PersonRepository personRepository,
            ApiConfig apiConfig) {
        this.webClient = webClient;
        this.personProcessor = personProcessor;
        this.personRepository = personRepository;
        this.apiConfig = apiConfig;
    }

    /**
     * Runs every Sunday & Thursday at midnight.
     */
    @Transactional
//    @Scheduled(cron = "0 0 0 ? * SUN,THU")
    public void updateMembers() {
        log.info("Scheduled job started: updateMembers(). Fetching current member IDs...");

        List<String> personIds = getPersonIdsToUpdate();
        log.info("Found {} personIds to update.", personIds.size());

        for (String personId : personIds) {
            log.debug("Updating member with personId={}", personId);

            try {
                String response = fetchMemberData(personId);
                if (response == null) {
                    log.warn("No response received for personId={}. Skipping.", personId);
                    continue;
                }

                Person updatedPerson = personProcessor.validatePerson(response);
                if (updatedPerson == null) {
                    log.warn("PersonProcessor returned null for personId={}. Skipping save.", personId);
                    continue;
                }

                personRepository.save(updatedPerson);
                log.info("Successfully updated Person with ID={}", updatedPerson.getPersonId());

            } catch (Exception e) {
                log.error("Failed to update Person for personId={}. Error: {}", personId, e.getMessage(), e);
            }
        }

        log.info("Scheduled job completed: updateMembers().");
    }

    private List<String> getPersonIdsToUpdate() {
        return personRepository.findAllCurrentMemberIds();
    }

    /**
     * Fetches member data from the Congress.gov API using WebClient.
     *
     * @param bioguideId The Bioguide ID of the member.
     * @return The raw JSON response as a String, or null if an error occurs.
     */
    private String fetchMemberData(String bioguideId) {
        log.debug("Fetching member data from Congress.gov for personId={}", bioguideId);

        try {
            return this.webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/member/{bioguideId}")
                            .queryParam("api_key", apiConfig.getKey())
                            .build(bioguideId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception ex) {
            log.error("Error fetching data from Congress.gov for personId={}: {}", bioguideId, ex.getMessage(), ex);
            return null;
        }
    }
}
