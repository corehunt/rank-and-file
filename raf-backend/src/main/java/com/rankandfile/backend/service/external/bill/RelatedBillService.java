package com.rankandfile.backend.service.external.bill;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.RelatedBillProcessor;
import com.rankandfile.backend.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class RelatedBillService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final RelatedBillProcessor relatedBillProcessor;

    public RelatedBillService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository,
            RelatedBillProcessor relatedBillProcessor) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.relatedBillProcessor = relatedBillProcessor;
    }

    /**
     * Fetches and processes related bills associated with a specific bill.
     *
     * @param congressNo The Congress number.
     * @param billType   The type of the bill (e.g., "hr", "s").
     * @param billNo     The bill number.
     */

    public void getRelatedBills(String congressNo, String billType, String billNo) {
        // Find the bill in the repository
        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        if (bill == null) {
            log.warn("Bill not found for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType);
            return;
        }

        log.info("Starting to fetch related bills for Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);

        try {
            String response = fetch(congressNo, billType, billNo);

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for related bills of Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);
                return;
            }

            relatedBillProcessor.processRelatedBills(response, bill);

            log.info("Related Bills successfully updated for Bill ID: {}", bill.getBillId());
            billRepository.save(bill);
            log.info("Related Bills successfully saved for Bill ID: {}", bill.getBillId());
        } catch (Exception e) {
            log.error("An error occurred while fetching related bills for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType, e);
            throw e;
        }

    }

    private String fetch(String congressNo, String billType, String billNo) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}/{billType}/{billNumber}/relatedbills")
                        .build(congressNo, billType.toLowerCase(), billNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
