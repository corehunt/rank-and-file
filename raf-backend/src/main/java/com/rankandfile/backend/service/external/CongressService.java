package com.rankandfile.backend.service.external;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rankandfile.backend.config.ApiConfig;
import com.rankandfile.backend.entity.Congress;
import com.rankandfile.backend.processor.CongressProcessor;
import com.rankandfile.backend.repository.CongressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class CongressService {

    private final WebClient webClient;
    private final ApiConfig apiConfig;
    private final CongressRepository congressRepository;
    private final CongressProcessor congressProcessor;

    @Autowired
    public CongressService(WebClient.Builder webClientBuilder, ApiConfig apiConfig, CongressRepository congressRepository, CongressProcessor congressProcessor) {
        this.webClient = webClientBuilder.baseUrl(apiConfig.getUrl()).build();
        this.apiConfig = apiConfig;
        this.congressRepository = congressRepository;
        this.congressProcessor = congressProcessor;
    }

    public List<Congress> fetchAndSaveCongressByNumber(String congressNo) {
        String response = this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("congress/{congressNo}")
                        .queryParam("api_key", apiConfig.getKey())
                        .build(congressNo))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject congressObject = responseObject.getAsJsonObject("congress");

        Congress congress = congressProcessor.processCongressData(congressObject);
        congressRepository.save(congress);

        List<Congress> congressList = new ArrayList<>();
        congressList.add(congress);

        return congressList;
    }

    public List<Congress> getAllCongresses() {
        return congressRepository.findAll();
    }

    public Congress getCongressById(Integer id) {
        return congressRepository.findById(id).orElse(null);
    }

}
