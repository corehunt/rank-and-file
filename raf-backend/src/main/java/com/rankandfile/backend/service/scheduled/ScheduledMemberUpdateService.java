package com.rankandfile.backend.service.scheduled;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.processor.PersonProcessor;
import com.rankandfile.backend.repository.PersonRepository;
import com.rankandfile.backend.config.ApiConfig;
import com.rankandfile.backend.util.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ScheduledMemberUpdateService {

    private final WebClient webClient;
    private final PersonProcessor personProcessor;
    private final PersonRepository personRepository;
    private final ApiConfig apiConfig;

    @Autowired
    public ScheduledMemberUpdateService(WebClient.Builder webClientBuilder, PersonProcessor personProcessor, PersonRepository personRepository, ApiConfig apiConfig) {
        this.webClient = webClientBuilder.baseUrl(apiConfig.getUrl()).build();
        this.personProcessor = personProcessor;
        this.personRepository = personRepository;
        this.apiConfig = apiConfig;
    }

    @Scheduled(cron = "0 0 0 * * *") // This cron expression schedules the task to run at midnight every day
    public void updateMembers() {
        List<String> bioguideIds = getBioguideIdsToUpdate();
        for (String bioguideId : bioguideIds) {
            updateMember(bioguideId);
        }
    }

    private List<String> getBioguideIdsToUpdate() {

        return List.of("A000376", "B001320"); // Example bioguideIds
    }

    private void updateMember(String bioguideId) {
        String response = this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/member/{bioguideId}")
                        .queryParam("api_key", apiConfig.getKey())
                        .build(bioguideId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Person updatedPerson = personProcessor.validatePerson(response);
        personRepository.save(updatedPerson);
    }
}
