package com.microservice.product.service.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, String description, BigDecimal price, int quantity) {

}
