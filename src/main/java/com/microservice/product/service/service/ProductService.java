package com.microservice.product.service.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.microservice.product.service.dto.ProductRequest;
import com.microservice.product.service.dto.ProductResponse;

public interface ProductService {

	ProductResponse createProduct(ProductRequest request);
    CompletableFuture<List<ProductResponse>> createProductsAsync(List<ProductRequest> requests);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    void rollbackFailedInventoryRequests(List<String> data); 
}
