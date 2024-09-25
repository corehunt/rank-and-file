package com.rankandfile.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
public class WebClientConfig {

    private final ApiConfig congressApiConfig;

    public WebClientConfig(ApiConfig congressApiConfig) {
        this.congressApiConfig = congressApiConfig;
    }

    @Bean
    public WebClient congressGovApiWebClient() {
        return WebClient.builder()
                .baseUrl(congressApiConfig.getUrl())
                .filter(addApiKeyQueryParamFilter())
                .build();
    }

    private ExchangeFilterFunction addApiKeyQueryParamFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            URI updatedUri = UriComponentsBuilder.fromUri(clientRequest.url())
                    .queryParam("api_key", congressApiConfig.getKey())
                    .build(true)
                    .toUri();

            ClientRequest updatedRequest = ClientRequest.from(clientRequest)
                    .url(updatedUri)
                    .build();

            return Mono.just(updatedRequest);
        });
    }
}