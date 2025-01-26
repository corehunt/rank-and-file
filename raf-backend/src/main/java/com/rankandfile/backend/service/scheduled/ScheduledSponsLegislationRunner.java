package com.rankandfile.backend.service.scheduled;

import com.rankandfile.backend.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ScheduledSponsLegislationRunner {

    private final ScheduledMemberSponsLegislationDiffService scheduledMemberSponsLegislationDiffService;
    private final PersonRepository personRepository;
    private static final List<String> sponsorTypes = Arrays.asList("Sponsor", "Co-Sponsor");

    public ScheduledSponsLegislationRunner(ScheduledMemberSponsLegislationDiffService service, PersonRepository personRepository) {
        this.scheduledMemberSponsLegislationDiffService = service;
        this.personRepository = personRepository;
    }

    /**
     * Scheduled task to hydrate sponsored & co-sponsored legislation relationship
     * Runs once a day at ??? time.
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    public void runMemberSponsLegislationUpdate() {
        // For testing, pick a single personId or multiple
//        String memberId = "B001309";  // Example

        int limit = 250;
        List<String> currentMemberList = personRepository.findAllCurrentMemberIds();

        for (String memberId : currentMemberList) {

            for(String sponsorType : sponsorTypes) {
                log.info("Scheduled: Updating {} legislation for personId={} ", sponsorType, memberId);

                try {
                    scheduledMemberSponsLegislationDiffService.getLegislationByPersonId(memberId, sponsorType, limit);
                } catch (Exception e) {
                    log.error("Error in runMemberSponsLegislationUpdate: {}", e.getMessage(), e);
                }
            }
        }

        log.info("Scheduled runMemberSponsLegislationUpdate Completed");
    }
}
