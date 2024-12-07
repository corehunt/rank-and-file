package com.rankandfile.backend.service.external.bill;

import com.rankandfile.backend.entity.Action;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillActionProcessor;
import com.rankandfile.backend.repository.ActionRepository;
import com.rankandfile.backend.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class BillActionService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final ActionRepository actionRepository;
    private final BillActionProcessor billActionProcessor;

    public BillActionService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository, ActionRepository actionRepository,
            BillActionProcessor billActionProcessor) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.actionRepository = actionRepository;
        this.billActionProcessor = billActionProcessor;
    }

    public List<Action> getActionsByBillNumber(String congressNo, String billType, String billNo, int limit) {
        List<Action> allActionsList = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        //TODO: implement bill creation in BillActionProcessor
        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        if (bill == null) {
            return Collections.emptyList();
        }

        log.info("Starting to fetch Actions for Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);

        try {
            while (hasMoreRecords) {
                final int currentOffset = offset;
                log.debug("Fetching Actions with offset: {}", currentOffset);

                String response = fetchActions(congressNo, billType, billNo, limit, currentOffset);

                if (response == null || response.isEmpty()) {
                    log.warn("Received empty response for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType);
                    break;
                }

                List<Action> actionList = billActionProcessor.processActionList(response, bill);
                allActionsList.addAll(actionList);

                // If the number of bills fetched is less than the limit, we've reached the end
                if (actionList.size() < limit) {
                    hasMoreRecords = false;
                }

                offset += limit;
            }

            log.info("Total Actions fetched: {}", allActionsList.size());
            actionRepository.saveAll(allActionsList);
            log.info("Actions successfully saved");
        } catch (Exception e) {
            log.error("An error occurred while fetching bills for Congress number: {}, Bill number {}, Bill Type: {}, error: {}", congressNo, billNo, billType, e);
            throw e;
        }

        return allActionsList;
    }

    private String fetchActions(String congressNo, String billType, String billNo, int limit, int offset) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}/{billType}/{billNumber}/actions")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(congressNo, billType.toLowerCase(), billNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}