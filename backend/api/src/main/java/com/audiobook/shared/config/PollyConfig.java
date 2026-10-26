package com.audiobook.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;

@Configuration
public class PollyConfig {

    @Value("${aws.region}")
    private String region;

    @Bean
    public PollyClient pollyClient() {
        return PollyClient.builder()
                .region(Region.of(region))
                .build();
    }
}
