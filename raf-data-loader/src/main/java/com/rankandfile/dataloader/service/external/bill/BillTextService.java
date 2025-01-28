package com.rankandfile.dataloader.service.external.bill;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.Text;
import com.rankandfile.dataloader.processor.BillTextProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.repository.TextRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class BillTextService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final BillTextProcessor billTextProcessor;
    private final TextRepository textRepository;

    public BillTextService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository,
            BillTextProcessor billTextProcessor, TextRepository textRepository) {
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.billTextProcessor = billTextProcessor;
        this.textRepository = textRepository;
    }

    public void fetchBillTexts(String congressNo, String billType, String billNumber) {

        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        if (bill == null) {
            log.error("Bill not found for Congress: {}, Bill Type: {}, Bill Number: {}", congressNo, billType, billNumber);
            throw new EntityNotFoundException("Bill not found");
        }

        log.info("Starting to fetch bill texts for Bill number: {}, Congress: {}, Bill Type: {}", billNumber, congressNo, billType);

        try {
            String response = fetchBillTextsFromApi(congressNo, billType, billNumber);

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for Congress: {}, Bill Number: {}, Bill Type: {}", congressNo, billNumber, billType);
                return;
            }

            List<Text> textList = billTextProcessor.processBillTextResponse(response, bill);
            log.info("Bill texts successfully fetched and processed for Bill ID: {}", bill.getBillId());

            log.info("Total Texts fetched and processed: {}", textList.size());
            textRepository.saveAll(textList);
            log.info("Texts successfully saved");

        } catch (Exception e) {
            log.error("An error occurred while fetching bill texts for Congress: {}, Bill Number: {}, Bill Type: {}, error: {}", congressNo, billNumber, billType, e.getMessage(), e);
            throw e;
        }
    }

    private String fetchBillTextsFromApi(String congressNo, String billType, String billNumber) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congress}/{billType}/{billNumber}/text")
                        .build(congressNo, billType.toLowerCase(), billNumber))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
