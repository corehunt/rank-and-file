package com.rankandfile.dataloader.service.external.congress;

import com.rankandfile.dataloader.entity.Congress;
import com.rankandfile.dataloader.processor.CongressProcessor;
import com.rankandfile.dataloader.repository.CongressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CongressService {

    private final WebClient webClient;
    private final CongressRepository congressRepository;
    private final CongressProcessor congressProcessor;

    public CongressService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            CongressRepository congressRepository,
            CongressProcessor congressProcessor) {
        this.webClient = webClient;
        this.congressRepository = congressRepository;
        this.congressProcessor = congressProcessor;
    }

    public List<Congress> fetchAndSaveCongressByNumber(String congressNo) {
        List<Congress> congressList = new ArrayList<>();

        try {
            log.info("Starting to fetch Congress history for congress number: {}", congressNo);

            String response = fetchCongress(congressNo);

            Congress congress = congressProcessor.processCongressData(response);
            congressList.add(congress);

            log.info("Successfully fetched Congress history for congress number: {}", congressNo);
            congressRepository.save(congress);
            log.info("Congress successfully saved.");
        } catch (Exception e) {
            log.error("An error occurred while fetching the Congress history for congress number: {}", congressNo, e);
            throw e;
        }

        return congressList;
    }

    private String fetchCongress(String congressNo) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("congress/{congressNo}")
                        .build(congressNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
