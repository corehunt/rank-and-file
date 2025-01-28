package com.rankandfile.dataloader.service.scheduled;

import com.google.common.util.concurrent.RateLimiter;
import com.rankandfile.dataloader.entity.Bill;
import com.rankandfile.dataloader.repository.BillRepository;
import com.rankandfile.dataloader.service.external.bill.*;
import com.rankandfile.dataloader.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBillHydrationRunner {

    private final BillRepository billRepository;
    private final BillByTypeAndNumberService billByTypeAndNumberService;
    private final BillActionService billActionService;
    private final BillCommitteeService billCommitteeService;
    private final BillSummaryService billSummaryService;
    private final BillTextService billTextService;
    private final BillSubjectService billSubjectService;
    private final RelatedBillService relatedBillService;

    private final int limit = 250;

    // Initialize RateLimiter with 1.388 permits per second (5000 / 3600 ≈ 1.388)
    private final RateLimiter rateLimiter = RateLimiter.create(1.388);

    /**
     * Scheduled task to hydrate recently created bills.
     * Runs once a day at ??? time.
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public void runBillHydration() {
        log.info("Starting scheduled bill hydration process");

        LocalDateTime since = LocalDateTime.now().minusHours(25); //25 hours to ensure enough overlap

        List<Bill> recentBills = billRepository.findByCreateTimestampAfter(since);
        log.info("Found {} bills created in the last 25 hours to hydrate.", recentBills.size());

        if (recentBills.isEmpty()) {
            log.info("No bills to hydrate at this time.");
            return;
        }

        int maxBillsPerHour = 650; // Safety margin (650 bills * 7 calls = 4550 calls) to stay under 5k rate limit
        int totalBills = recentBills.size();
        int totalBatches = (int) Math.ceil((double) totalBills / maxBillsPerHour);

        log.info("Total batches to process: {}", totalBatches);

        for (int batchNumber = 1; batchNumber <= totalBatches; batchNumber++) {
            int startIndex = (batchNumber - 1) * maxBillsPerHour;
            int endIndex = Math.min(startIndex + maxBillsPerHour, totalBills);
            List<Bill> batchBills = recentBills.subList(startIndex, endIndex);

            log.info("Processing batch {}/{}: {} bills.", batchNumber, totalBatches, batchBills.size());

            for (Bill bill : batchBills) {
                try {
                    log.info("Hydrating bill: CongressNo={}, BillType={}, BillNumber={}", bill.getCongress(), bill.getBillType(), bill.getBillNo());
                    String congressNo = bill.getCongress();
                    String billType = bill.getBillType();
                    String billNo = bill.getBillNo();

                    // Sequentially call the endpoints in the specified order with rate limiting
                    hydrateBillData(congressNo, billType, billNo);

                    log.info("Successfully hydrated bill ID: {}", bill.getBillId());

                } catch (TooManyRequestsException e) {
                    log.warn("Received 429 Too Many Requests. Pausing hydration for 10 minutes.");
                    pauseHydration(Duration.ofMinutes(10));

                    // After pausing, retry the current bill once
                    try {
                        hydrateBillData(bill.getCongress(), bill.getBillType(), bill.getBillNo());
                        log.info("Successfully hydrated bill ID after retry: {}", bill.getBillId());
                    } catch (TooManyRequestsException retryException) {
                        log.error("Retry failed for bill ID: {} - {}", bill.getBillId(), retryException.getMessage(), retryException);
                    } catch (Exception retryOtherException) {
                        log.error("Error hydrating bill ID: {} during retry - {}", bill.getBillId(), retryOtherException.getMessage(), retryOtherException);
                    }
                } catch (Exception e) {
                    log.error("Error hydrating bill ID: {} - {}", bill.getBillId(), e.getMessage(), e);
                }
            }

            // After processing a batch, if not the last batch, pause to respect rate limits
            if (batchNumber < totalBatches) {
                log.info("Completed batch {}/{}. Pausing for 1 hour to respect API rate limits.", batchNumber, totalBatches);
                pauseHydration(Duration.ofHours(1));
            }
        }

        log.info("Completed scheduled bill hydration process");
    }

    /**
     * Hydrates all necessary data for a single bill, respecting the rate limit.
     *
     * @param congressNo The congress number.
     * @param billType   The type of the bill.
     * @param billNo     The bill number.
     * @throws TooManyRequestsException if a 429 status is received.
     */
    private void hydrateBillData(String congressNo, String billType, String billNo) throws TooManyRequestsException {
        callServiceWithRateLimit(() -> billByTypeAndNumberService.getBillByTypeAndNumber(congressNo, billType, billNo));
        callServiceWithRateLimit(() -> billActionService.getActionsByBillNumber(congressNo, billType, billNo, limit));
        callServiceWithRateLimit(() -> billCommitteeService.getCommitteesByBillNumber(congressNo, billType, billNo));
        callServiceWithRateLimit(() -> billSummaryService.fetchBillSummary(congressNo, billType, billNo));
        callServiceWithRateLimit(() -> billTextService.fetchBillTexts(congressNo, billType, billNo));
        callServiceWithRateLimit(() -> billSubjectService.fetchBillSubjects(congressNo, billType, billNo));
        callServiceWithRateLimit(() -> relatedBillService.getRelatedBills(congressNo, billType, billNo));
    }

    /**
     * Wraps service calls with rate limiting and handles 429 errors.
     *
     * @param serviceCall The service call to execute.
     * @throws TooManyRequestsException if a 429 status is received.
     */
    private void callServiceWithRateLimit(ServiceCall serviceCall) throws TooManyRequestsException {
        rateLimiter.acquire(); // Ensures 1.388 permits/sec
        try {
            serviceCall.execute();
        } catch (TooManyRequestsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("API call failed.", e);
        }
    }

    /**
     * Pauses the hydration process for the specified duration.
     *
     * @param duration The duration to pause.
     */
    private void pauseHydration(Duration duration) {
        try {
            log.info("Pausing hydration for {} minutes.", duration.toMinutes());
            Thread.sleep(duration.toMillis());
            log.info("Resuming hydration after pause.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Hydration pause interrupted: {}", e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface ServiceCall {
        void execute() throws Exception;
    }
}
