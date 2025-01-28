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
public class CongressClassPersonService {

    private final WebClient webClient;
    private final CongressMemberProcessor congressMemberProcessor;
    private final PersonRepository personRepository;

    public CongressClassPersonService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            CongressMemberProcessor congressMemberProcessor,
            PersonRepository personRepository) {
        this.webClient = webClient;
        this.congressMemberProcessor = congressMemberProcessor;
        this.personRepository = personRepository;
    }

    public List<Person> fetchMembersOfCurrentCongress(String congressNo, int limit) {
        List<Person> allMembers = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        log.info("Starting to fetching members for Congress number: {}", congressNo);

        try {
            while (hasMoreRecords) {
                int currentOffset = offset;
                log.debug("Fetching members with offset: {},", currentOffset);

                String response = fetchMembers(congressNo, limit, offset);

                if(response == null || response.isEmpty()) {
                    log.warn("Received empty response for Congress number: {}, offset: {}", congressNo, currentOffset);
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
            personRepository.saveAll(allMembers);
            log.info("Members successfully saved.");

        } catch (Exception e) {
            log.error("An error occurred while fetching and processing members for Congress number: {}", congressNo, e);
            throw e;
        }

        return allMembers;
    }

    private String fetchMembers(String congressNo, int limit, int offset) {
         return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("member/congress/{congress}")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(congressNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
