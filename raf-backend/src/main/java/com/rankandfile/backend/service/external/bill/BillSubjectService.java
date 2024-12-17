package com.rankandfile.backend.service.external.bill;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillSubjectProcessor;
import com.rankandfile.backend.repository.BillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class BillSubjectService {

    private final WebClient webClient;
    private final BillSubjectProcessor billSubjectProcessor;
    private final BillRepository billRepository;

    public BillSubjectService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillSubjectProcessor billSubjectProcessor,
            BillRepository billRepository) {
        this.webClient = webClient;
        this.billSubjectProcessor = billSubjectProcessor;
        this.billRepository = billRepository;
    }

    public void fetchBillSubjects(String congressNo, String billType, String billNumber) {

        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNumber, billType);
        if (bill == null) {
            log.error("Bill not found for Congress: {}, Bill Type: {}, Bill Number: {}", congressNo, billType, billNumber);
            throw new EntityNotFoundException("Bill not found");
        }

        log.info("Starting to fetch bill subject for Bill number: {}, Congress: {}, Bill Type: {}", billNumber, congressNo, billType);

        try {
            String response = fetchBillSubjectsFromApi(congressNo, billType, billNumber);

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for Congress: {}, Bill Number: {}, Bill Type: {}", congressNo, billNumber, billType);
                return;
            }

            String subjectString = billSubjectProcessor.processLegislativeSubjects(response, bill.getBillId());

            if(subjectString != null && !subjectString.isEmpty()) {
                bill.setLegislativeSubjects(subjectString);
            }
            billRepository.save(bill);
            log.info("subject successfully saved for billId: {}", bill.getBillId());
        } catch (Exception e) {
            log.error("An error occurred while fetching bill subjects for Congress: {}, Bill Number: {}, Bill Type: {}, error: {}", congressNo, billNumber, billType, e.getMessage(), e);
            throw e;
        }
    }

    private String fetchBillSubjectsFromApi(String congressNo, String billType, String billNumber) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congress}/{billType}/{billNumber}/subjects")
                        .build(congressNo, billType.toLowerCase(), billNumber))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
