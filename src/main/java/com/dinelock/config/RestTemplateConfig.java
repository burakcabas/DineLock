package com.dinelock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // Creates a global RestTemplate bean for making HTTP requests to external APIs
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}