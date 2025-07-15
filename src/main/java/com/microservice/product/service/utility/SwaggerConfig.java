package com.microservice.product.service.utility;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Product API", version = "1.0", description = "Product Management Microservice")
)
public class SwaggerConfig {
    // Springdoc auto-scans endpoints
}
