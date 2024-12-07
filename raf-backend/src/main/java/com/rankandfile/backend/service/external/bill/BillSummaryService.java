package com.rankandfile.backend.service.external.bill;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillSummaryProcessor;
import com.rankandfile.backend.repository.BillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class BillSummaryService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final BillSummaryProcessor billSummaryProcessor;

    public BillSummaryService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository,
            BillSummaryProcessor billSummaryProcessor) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.billSummaryProcessor = billSummaryProcessor;
    }

    public void fetchBillSummary(String congressNo, String billType, String billNumber) {
        // Retrieve the Bill entity from the database
        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        if (bill == null) {
            log.error("Bill not found for Congress: {}, Bill Type: {}, Bill Number: {}", congressNo, billType, billNumber);
            throw new EntityNotFoundException("Bill not found");
        }

        log.info("Starting to fetch summary for Bill number: {}, Congress: {}, Bill Type: {}", billNumber, congressNo, billType);

        try {
            String response = fetchSummary(congressNo, billType, billNumber);

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for Congress: {}, Bill Number: {}, Bill Type: {}", congressNo, billNumber, billType);
                return;
            }

            billSummaryProcessor.processBillSummaryResponse(response, bill);
            log.info("Summary successfully fetched and processed for Bill ID: {}", bill.getBillId());
            billRepository.save(bill);

        } catch (Exception e) {
            log.error("An error occurred while fetching summary for Congress: {}, Bill Number: {}, Bill Type: {}, error: {}", congressNo, billNumber, billType, e.getMessage(), e);
            throw e;
        }
    }

    private String fetchSummary(String congressNo, String billType, String billNumber) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congress}/{billType}/{billNumber}/summaries")
                        .build(congressNo, billType.toLowerCase(), billNumber))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
