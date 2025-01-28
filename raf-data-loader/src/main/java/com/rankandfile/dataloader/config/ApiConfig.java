package com.rankandfile.dataloader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "congress.api")
@Data
public class ApiConfig {

    private String key;

    private String url;

}
