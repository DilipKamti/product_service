package com.microservice.product.service.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.microservice.product.service.dto.ProductRequest;
import com.microservice.product.service.dto.ProductResponse;
import com.microservice.product.service.model.Product;
import com.microservice.product.service.repository.ProductRepository;
import com.microservice.product.service.utility.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

        private final ProductRepository productRepository;

        @Override
        public ProductResponse createProduct(ProductRequest request) {
                Product product = Product.builder()
                                .name(request.name())
                                .productId(generateUniqueProductId())
                                .description(request.description())
                                .price(request.price())
                                .quantity(request.quantity())
                                .build();

                Product savedProduct = productRepository.save(product);
                log.info("Product created with ID: {}", savedProduct.getId());

                return mapToResponse(savedProduct);
        }

        @Async
        @Override
        public CompletableFuture<List<ProductResponse>> createProductsAsync(List<ProductRequest> requests) {
                List<Product> products = requests.stream()
                                .map(req -> Product.builder()
                                                .name(req.name())
                                                .productId(generateUniqueProductId())
                                                .description(req.description())
                                                .price(req.price())
                                                .quantity(req.quantity())
                                                .build())
                                .toList();

                List<Product> saved = productRepository.saveAll(products);
                log.info("Async created {} products", saved.size());

                List<ProductResponse> response = saved.stream()
                                .map(this::mapToResponse)
                                .toList();

                return CompletableFuture.completedFuture(response);
        }

        @Override
        public ProductResponse getProductById(Long id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

                return mapToResponse(product);
        }

        private ProductResponse mapToResponse(Product product) {
                return new ProductResponse(
                                product.getId(),
                                product.getProductId(),
                                product.getName(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getQuantity()
                );
        }

        @Override
        public Page<ProductResponse> getAllProducts(Pageable pageable) {
                return productRepository.findAll(pageable)
                                .map(this::mapToResponse);
        }

        private String generateUniqueProductId() {
                return "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
}
