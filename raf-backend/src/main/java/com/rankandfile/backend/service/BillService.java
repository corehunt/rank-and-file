package com.rankandfile.backend.service;

import com.rankandfile.backend.config.ApiConfig;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.processor.BillByCongressProcessor;
import com.rankandfile.backend.processor.BillByCongressTypeNumberProcessor;
import com.rankandfile.backend.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class BillService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillService.class);

    private final WebClient webClient;
    private final ApiConfig apiConfig;
    private final BillRepository billRepository;
    private final BillByCongressProcessor billByCongressProcessor;
    private final BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor;

    @Autowired
    public BillService(WebClient.Builder webClientBuilder, ApiConfig apiConfig, BillRepository billRepository, BillByCongressProcessor billByCongressProcessor, BillByCongressTypeNumberProcessor billByCongressTypeNumberProcessor) {
        this.webClient = webClientBuilder.baseUrl(apiConfig.getUrl()).build();
        this.apiConfig = apiConfig;
        this.billRepository = billRepository;
        this.billByCongressProcessor = billByCongressProcessor;
        this.billByCongressTypeNumberProcessor = billByCongressTypeNumberProcessor;
    }

    public List<Bill> getBillsByCongress(Integer congressNo){
        List<Bill> allBillsByCongress = new ArrayList<>();
//        int limit = 250;
//        int offset = 0;
//        boolean hasMoreRecords = true;

//        while (hasMoreRecords) {
//            int finalOffset = offset;
//              **return code block here**
//            offset += limit;
//
//            // If the number of persons fetched is less than the limit, we've reached the end
//            if (billList.size() < limit) {
//                hasMoreRecords = false;
//            }
//        }

        String response = this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}")
                        .queryParam("api_key", apiConfig.getKey())
//                        .queryParam("limit", limit)
//                        .queryParam("offset", finalOffset)
                        .build(congressNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Bill> billList = billByCongressProcessor.processBillList(response);
        allBillsByCongress.addAll(billList);

        for(Bill bill : allBillsByCongress) {
            LOGGER.info("Bill processed: {}", bill);
            billRepository.save(bill);
        }

        LOGGER.info("Bills processed, returning bill list");
        return allBillsByCongress;

    }

    public Bill getBillByTypeAndNumber(Integer congressNo, String billType, Integer billNo) {

        String response = this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("bill/{congressNo}/{billType}/{billNumber}")
                        .queryParam("api_key", apiConfig.getKey())
                        .build(congressNo, billType, billNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Bill bill = billByCongressTypeNumberProcessor.process(response);
        LOGGER.info("Bill number saved: {} for object: {}", bill.getBillNo(), bill);
        billRepository.save(bill);

        return bill;
    }


}
