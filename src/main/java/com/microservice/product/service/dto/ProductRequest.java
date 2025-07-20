package com.microservice.product.service.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
		@NotBlank(message = "Product name is required") @Schema(description = "Name of the product", example = "Laptop") String name,
		@Schema(description = "Product description") String description,
		@NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") @Schema(description = "Price of the product", example = "499.99") BigDecimal price) {

}
