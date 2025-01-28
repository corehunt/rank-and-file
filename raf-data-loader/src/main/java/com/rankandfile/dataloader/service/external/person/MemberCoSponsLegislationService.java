package com.rankandfile.dataloader.service.external.person;

import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.processor.SponsoredLegislationProcessor;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MemberCoSponsLegislationService {

    private final WebClient webClient;
    private final SponsoredLegislationProcessor sponsoredLegislationProcessor;
    private final SponsoredLegislationRepository sponsoredLegislationRepository;

    public MemberCoSponsLegislationService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            SponsoredLegislationProcessor sponsoredLegislationProcessor,
            SponsoredLegislationRepository sponsoredLegislationRepository) {
        this.webClient = webClient;
        this.sponsoredLegislationProcessor = sponsoredLegislationProcessor;
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
    }

    public List<SponsoredLegislation> getCoSponsoredLegislationByPersonId(String personId, int limit) {
        List<SponsoredLegislation> allSponsLegList = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        log.info("Starting to fetch Co-Sponsored Legislation for Member: {}", personId);

        try {
            while (hasMoreRecords) {
                final int currentOffset = offset;
                log.info("Fetching co sponsored legislation for personId: {}, offset: {}", personId, currentOffset);

                String response = fetchSponsLegislation(personId, currentOffset, limit);

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
            }

            log.info("Total fetched and processed Co-Sponsored Legislation: {}, for personId: {}", allSponsLegList.size(), personId);
            sponsoredLegislationRepository.saveAll(allSponsLegList);
            log.info("Co-Sponsored Legislation successfully saved.");

        } catch (Exception e) {
            log.error("An error occurred while processing Co-Sponsored Legislation for personId: {}", personId, e);
        }

        return allSponsLegList;
    }

    private String fetchSponsLegislation(String personId, int offset, int limit) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("member/{personId}/cosponsored-legislation")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(personId))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
