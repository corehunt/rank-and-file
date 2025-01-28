package com.rankandfile.dataloader.service.external.bill;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.processor.BillCommitteeProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class BillCommitteeService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final BillCommitteeProcessor billCommitteeProcessor;

    public BillCommitteeService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository,
            BillCommitteeProcessor billCommitteeProcessor) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.billCommitteeProcessor = billCommitteeProcessor;
    }

    /**
     * Fetches and processes committees associated with a specific bill.
     *
     * @param congressNo The Congress number.
     * @param billType   The type of the bill (e.g., "hr", "s").
     * @param billNo     The bill number.
     */
    public void getCommitteesByBillNumber(String congressNo, String billType, String billNo) {
        // Find the bill in the repository
        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        if (bill == null) {
            log.warn("Bill not found for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType);
            return;
        }

        log.info("Starting to fetch committees for Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);

        try {
            String response = fetchCommittees(congressNo, billType, billNo);

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for committees of Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);
                return;
            }

            // Process the response to update the Bill-Committee relationships
            billCommitteeProcessor.process(response, bill.getBillId());

            log.info("Committees successfully updated for Bill ID: {}", bill.getBillId());
            billRepository.save(bill);
            log.info("Committees successfully saved for Bill ID: {}", bill.getBillId());

        } catch (Exception e) {
            log.error("An error occurred while fetching committees for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType, e);
            throw e;
        }
    }

    private String fetchCommittees(String congressNo, String billType, String billNo) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}/{billType}/{billNumber}/committees")
                        .build(congressNo, billType.toLowerCase(), billNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
