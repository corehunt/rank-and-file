package com.rankandfile.dataloader.service.external.bill;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.processor.BillByCongressProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BillByCongressService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final BillByCongressProcessor billByCongressProcessor;

    public BillByCongressService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository,
            BillByCongressProcessor billByCongressProcessor) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.billByCongressProcessor = billByCongressProcessor;
    }

    public List<Bill> getBillsByCongress(String congressNo, int limit) {
        List<Bill> allBillsByCongress = new ArrayList<>();
        int offset = 0;
        boolean hasMoreRecords = true;

        log.info("Starting to fetch bills for Congress number: {}", congressNo);

        try {
            while (hasMoreRecords) {
                final int currentOffset = offset;
                log.debug("Fetching bills with offset: {}", currentOffset);

                String response = fetchBills(congressNo, currentOffset, limit)  ;

                if (response == null || response.isEmpty()) {
                    log.warn("Received empty response for Congress number: {}, offset: {}", congressNo, currentOffset);
                    break;
                }

                List<Bill> billList = billByCongressProcessor.processBillList(response);

                if (billList == null || billList.isEmpty()) {
                    log.info("No bills found for Congress number: {}, offset: {}", congressNo, currentOffset);
                    break;
                }

                allBillsByCongress.addAll(billList);

                if (billList.size() < limit) {
                    hasMoreRecords = false;
                } else {
                    offset += limit;
                }
            }

            log.info("Total bills fetched and processed: {}", allBillsByCongress.size());
            billRepository.saveAll(allBillsByCongress);
            log.info("Bills successfully saved.");

        } catch (Exception e) {
            log.error("An error occurred while fetching bills for Congress number: {}", congressNo, e);
            throw e;
        }

        return allBillsByCongress;
    }

    private String fetchBills(String congressNo, int offset, int limit) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build(congressNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}