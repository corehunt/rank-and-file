package com.rankandfile.dataloader.service.external.person;

import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.processor.CongressMemberProcessor;
import com.rankandfile.dataloader.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MembersService {

    private final WebClient webClient;
    private final CongressMemberProcessor congressMemberProcessor;
    private final PersonRepository personRepository;

    public MembersService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            CongressMemberProcessor congressMemberProcessor,
            PersonRepository personRepository) {
        this.webClient = webClient;
        this.congressMemberProcessor = congressMemberProcessor;
        this.personRepository = personRepository;
    }

    public List<Person> fetchAndSaveMembers(int limit) {
        List<Person> allMembers = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        log.info("Starting to fetch all members");

        try {
            while (hasMoreRecords) {
                int currentOffset = offset;
                log.debug("Fetching members from offset {}", currentOffset);

                String response = fetchMembers(limit, offset);

                if (response == null || response.isEmpty()) {
                    log.warn("Received empty response for loading all members");
                    hasMoreRecords = false;
                    continue;
                }

                List<Person> persons = congressMemberProcessor.processMembers(response);

                allMembers.addAll(persons);

                if (persons.size() < limit) {
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                }
            }

            log.info("Total members fetched: {}", allMembers.size());
            if (!allMembers.isEmpty()) {
                personRepository.saveAll(allMembers);
                log.info("Members successfully saved");
            } else {
                log.info("No members to save");
            }

        } catch (Exception e) {
            log.error("An error occurred while fetching and processing members", e);
            throw e;
        }

        return allMembers;
    }

    private String fetchMembers(int limit, int offset) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("member")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
