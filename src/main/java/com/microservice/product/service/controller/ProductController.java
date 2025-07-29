package com.microservice.product.service.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.product.service.dto.ApiResponse;
import com.microservice.product.service.dto.ProductRequest;
import com.microservice.product.service.dto.ProductResponse;
import com.microservice.product.service.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Product Controller", description = "APIs for managing products")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a product")
	public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
		ProductResponse response = productService.createProduct(request);
		return ResponseEntity.ok(ApiResponse.success(response, "Product created successfully"));
	}

	@PostMapping("/batch")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create products in batch")
	public ResponseEntity<ApiResponse<List<ProductResponse>>> createBatch(@Valid @RequestBody List<@Valid ProductRequest> requests) throws InterruptedException, ExecutionException{
		CompletableFuture<List<ProductResponse>> future = productService.createProductsAsync(requests);
        List<ProductResponse> responseList = future.get();

        return ResponseEntity.ok(ApiResponse.success(responseList, "Products created successfully"));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@Operation(summary = "Get product by ID")
	public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
		ProductResponse response = productService.getProductById(id);
		return ResponseEntity.ok(ApiResponse.success(response, "Product fetched successfully"));
	}

	@GetMapping
	//@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@Operation(summary = "Get all products with pagination")
	public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products, "All products fetched"));
    }
}
