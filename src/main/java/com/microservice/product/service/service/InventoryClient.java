package com.microservice.product.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.microservice.product.service.dto.InventoryRequest;
import com.microservice.product.service.dto.InventoryResponse;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${inventory.service.base-url:http://inventory-service:8084}")
    private String inventoryServiceBaseUrl;

    public List<InventoryResponse> addInventoryBatch(List<InventoryRequest> requests) {
        log.info("Sending request to Inventory Service for batch insertion...");

        return webClientBuilder.build()
                .post()
                .uri(inventoryServiceBaseUrl + "/api/v1/inventory/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requests)
                .retrieve()
                .bodyToFlux(InventoryResponse.class) // assumes API returns a List<InventoryResponse>
                .collectList()
                .block(); // Blocking since this is not reactive-service (can be replaced with async if needed)
    }
}

