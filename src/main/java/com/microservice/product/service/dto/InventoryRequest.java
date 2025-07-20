package com.microservice.product.service.dto;

public record InventoryRequest(
    String productCode,
    int quantity
) {}