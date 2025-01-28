package com.rankandfile.dataloader.service.external.person;

import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.processor.PersonProcessor;
import com.rankandfile.dataloader.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class IndividualMemberService {

    private final WebClient webClient;
    private final PersonProcessor personProcessor;
    private final PersonRepository personRepository;

    public IndividualMemberService(
            PersonProcessor personProcessor,
            @Qualifier("congressGovApiWebClient") WebClient webClient, PersonRepository personRepository) {
        this.personProcessor = personProcessor;
        this.webClient = webClient;
        this.personRepository = personRepository;
    }

    public Person fetchAndProcessPerson(String bioguideId) {
        if (bioguideId == null || bioguideId.isEmpty()) {
            log.error("Invalid bioguideId: {}", bioguideId);
            throw new IllegalArgumentException("bioguideId cannot be null or empty");
        }

        log.info("Fetching person data for bioguideId: {}", bioguideId);

        try {
            String response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/member/{bioguideId}")
                            .build(bioguideId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for bioguideId: {}", bioguideId);
                return null;
            }

            log.debug("Received response: {}", response);

            Person person = personProcessor.validatePerson(response);

            if (person == null) {
                log.error("PersonProcessor returned null for bioguideId: {}", bioguideId);
                throw new RuntimeException("Failed to process person data");
            }

            personRepository.save(person);
            log.info("Person saved successfully with ID: {}", person.getPersonId());

            return person;

        } catch (Exception e) {
            log.error("An error occurred while fetching and processing person data for bioguideId: {} - {}",
                    bioguideId, e.getMessage(), e);
            throw e;
        }
    }
}