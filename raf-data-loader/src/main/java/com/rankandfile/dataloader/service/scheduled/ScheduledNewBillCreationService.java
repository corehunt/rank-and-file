package com.rankandfile.dataloader.service.scheduled;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.processor.RecentBillProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScheduledNewBillCreationService {

    private final WebClient webClient;
    private final RecentBillProcessor recentBillProcessor;
    private final BillRepository billRepository;
    private final ScheduledBillHydrationRunner hydrationRunner;

    public ScheduledNewBillCreationService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            RecentBillProcessor recentBillProcessor,
            BillRepository billRepository, ScheduledBillHydrationRunner hydrationRunner) {
        this.webClient = webClient;
        this.recentBillProcessor = recentBillProcessor;
        this.billRepository = billRepository;
        this.hydrationRunner = hydrationRunner;
    }

    /**
     * Scheduled job to load and hydrate bills with a last action date captured within 1 hour previous
     * Runs every day at the top of every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void loadRecentBills() {
        List<Bill> recentBills = new ArrayList<>();
        int offset = 0;
        int limit = 250;
        boolean hasMoreRecords = true;

        Instant now = Instant.now();
        Instant twoHoursAgo = now.minus(1, ChronoUnit.HOURS);
        log.info("starting to fetch bills for last timestamp between: {} and {}", now, twoHoursAgo);

        // Format the timestamps in the format: YYYY-MM-DDTHH:mm:ssZ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);
        String toDateTime = formatter.format(now);
        String fromDateTime = formatter.format(twoHoursAgo);

        try {
            while(hasMoreRecords){
                final int currentOffset = offset;
                log.debug("fetching bills with offset: {}", currentOffset);

                String response = fetchRecentBills(limit, offset, toDateTime, fromDateTime);

                if(response == null || response.isEmpty()){
                    log.warn("Received empty response, exiting scheduled service");
                    break;
                }

                List<Bill> processedBillList = recentBillProcessor.processRecentBills(response);
                if(processedBillList == null || processedBillList.isEmpty()){
                    break;
                }

                recentBills.addAll(processedBillList);

                if(processedBillList.size() < limit){
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                }
            }

            billRepository.saveAll(recentBills);
            log.info("saved {} recent bills to database", recentBills.size());

            hydrationRunner.runBillHydration(now);

        } catch (Exception e) {
            log.error("Error while fetching recent bills", e);
            throw e;
        }


    }

    private String fetchRecentBills(int limit, int offset, String toDateTime, String fromDateTime) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .queryParam("fromDateTime", fromDateTime)
                        .queryParam("toDateTime", toDateTime)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
