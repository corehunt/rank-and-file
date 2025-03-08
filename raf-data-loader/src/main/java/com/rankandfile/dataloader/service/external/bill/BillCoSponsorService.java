package com.rankandfile.dataloader.service.external.bill;

import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.entity.SponsoredLegislation;
import com.rankandfile.dataloader.processor.SponsoredLegislationProcessor;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.repository.SponsoredLegislationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class BillCoSponsorService {

    private final WebClient webClient;
    private final BillRepository billRepository;
    private final SponsoredLegislationRepository sponsoredLegislationRepository;
    private final SponsoredLegislationProcessor sponsoredLegislationProcessor;

    public BillCoSponsorService(
            @Qualifier("congressGovApiWebClient") WebClient webClient,
            BillRepository billRepository, SponsoredLegislationRepository sponsoredLegislationRepository,
            SponsoredLegislationProcessor sponsoredLegislationProcessor){
        this.webClient = webClient;
        this.billRepository = billRepository;
        this.sponsoredLegislationRepository = sponsoredLegislationRepository;
        this.sponsoredLegislationProcessor = sponsoredLegislationProcessor;
    }

    /**
     * Fetches and processes co-sponsors associated with a specific bill.
     *
     * @param congressNo The Congress number.
     * @param billType   The type of the bill (e.g., "hr", "s").
     * @param billNo     The bill number.
     */
    public void getCoSponsorsByBillNumber(String congressNo, String billType, String billNo) {
        Bill bill = billRepository.findByCongressAndBillNoAndBillType(congressNo, billNo, billType);
        if (bill == null) {
            log.warn("Bill not found for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType);
            return;
        }

        log.info("Starting to fetch co-sponsors for Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);

        try {
            String response = fetchCoSponsors(congressNo, billType, billNo);

            if (response == null || response.isEmpty()) {
                log.warn("Received empty response for committees of Bill number: {}, Congress: {}, Bill Type: {}", billNo, congressNo, billType);
                return;
            }

            List<SponsoredLegislation> sponsoredLegislationList = sponsoredLegislationProcessor.process(response, bill);

            log.info("CoSponsors successfully updated for Bill ID: {}", bill.getBillId());
            billRepository.save(bill);
            sponsoredLegislationRepository.saveAll(sponsoredLegislationList);
            log.info("CoSponsors successfully saved for Bill ID: {}", bill.getBillId());

        } catch (Exception e) {
            log.error("An error occurred while fetching CoSponsors for Congress number: {}, Bill number: {}, Bill Type: {}", congressNo, billNo, billType, e);
            throw e;
        }
    }

    private String fetchCoSponsors(String congressNo, String billType, String billNo) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}/{billType}/{billNumber}/cosponsors")
                        .build(congressNo, billType.toLowerCase(), billNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
