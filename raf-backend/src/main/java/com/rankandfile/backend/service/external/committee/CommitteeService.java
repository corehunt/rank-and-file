package com.rankandfile.backend.service.external.committee;

import com.rankandfile.backend.entity.Committee;
import com.rankandfile.backend.processor.CommitteeProcessor;
import com.rankandfile.backend.repository.CommitteeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CommitteeService {

    private final WebClient webClient;
    private final CommitteeProcessor committeeProcessor;
    private final CommitteeRepository committeeRepository;

    public CommitteeService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            CommitteeProcessor committeeProcessor,
            CommitteeRepository committeeRepository) {
        this.webClient = webClient;
        this.committeeProcessor = committeeProcessor;
        this.committeeRepository = committeeRepository;
    }

    public List<Committee> fetchAndProcessCommittees(int limit) {
        List<Committee> allCommittees = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        log.info("Starting to fetch committees");

        try {
            while(hasMoreRecords) {
                int currentOffset = offset;
                log.debug("Fetching committees from offset {}", currentOffset);

                String response = fetchCommittees(limit, offset);

                if(response == null || response.isEmpty()) {
                    log.warn("Received empty response for committees");
                    hasMoreRecords = false;
                    continue;
                }

                List<Committee> committees = committeeProcessor.process(response);

                allCommittees.addAll(committees);

                if(committees.size() < limit) {
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                }
            }
        } catch (Exception e) {
            log.error("Error while fetching committees", e);
            throw e;
        }

        log.info("Total Committees fetched {}", allCommittees.size());
        committeeRepository.saveAll(allCommittees);
        log.info("Committees successfully saved");

        return allCommittees;
    }

    private String fetchCommittees(int limit, int offset) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("committee")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
