package com.rankandfile.dataloader.service.external.bill;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.processor.BillByCongressTypeNumberProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class BillByTypeAndNumberService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    public BillByTypeAndNumberService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository,
            BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.billByCongressTypeNumberProcessor = billByCongressTypeNumberProcessor;
    }

    public Bill getBillByTypeAndNumber(String congressNo, String billType, String billNo) {

        try {
            String response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}/{billType}/{billNumber}")
                            .build(congressNo, billType, billNo))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isEmpty()) {
                log.warn("Did not receive bill data for congress #: {}, billType: {}, billNumber: {}", congressNo, billType, billNo);
                return null;
            }

            Bill bill = billByCongressTypeNumberProcessor.process(response);
            log.info("Bill successfully processed");
            billRepository.save(bill);
            log.info("Bill successfully saved, BILL_ID: {}", bill.getBillId() );

            return bill;
        } catch (Exception e) {
            log.error("An error occurred while fetching bill data for bill #: {}, congress #: {} - {}", billNo, congressNo, e.getMessage());
            throw e;
        }

    }
}