package com.microservice.product.service.utility;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // WebClient.Builder Bean (used for non-blocking reactive calls)
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // RestTemplate Bean (used for traditional blocking calls)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

