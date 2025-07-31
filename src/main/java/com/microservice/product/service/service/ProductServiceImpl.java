package com.microservice.product.service.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.microservice.product.service.dto.InventoryRequest;
import com.microservice.product.service.dto.ProductRequest;
import com.microservice.product.service.dto.ProductResponse;
import com.microservice.product.service.kafka.KafkaProducerService;
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
        
        //private final InventoryClient inventoryClient; // Uncomment if using Feign/rest/webclient for inventory service
        private final KafkaProducerService kafkaProducerService;

        @Override
        public ProductResponse createProduct(ProductRequest request) {
                Product product = Product.builder()
                                .name(request.name())
                                .productId(generateUniqueProductId())
                                .description(request.description())
                                .price(request.price())
                                .build();

                Product savedProduct = productRepository.save(product);
                log.info("Product created with ID: {}", savedProduct.getId());

                return mapToResponse(savedProduct);
        }
        
        /*
         * Uncomment this method if you want to implement async product creation using webclient or Feign client or rest template
         * 
        @Async
        @Override
        public CompletableFuture<List<ProductResponse>> createProductsAsync(List<ProductRequest> requests) {
                try {
                        List<Product> products = requests.stream()
                                        .map(req -> Product.builder()
                                                        .name(req.name())
                                                        .productId(generateUniqueProductId())
                                                        .description(req.description())
                                                        .price(req.price())
                                                        .build())
                                        .toList();

                        List<Product> saved = productRepository.saveAll(products);
                        log.info("Async created {} products", saved.size());

                        List<ProductResponse> response = saved.stream()
                                        .map(this::mapToResponse)
                                        .toList();

                        List<InventoryRequest> inventoryRequests = response.stream()
                                        .map(res -> new InventoryRequest(res.productId(), 1))
                                        .toList();

                        // Call inventory service with exception handling
                        try {
                                List<InventoryResponse> inventoryResponses = inventoryClient
                                                .addInventoryBatch(inventoryRequests);
                                inventoryResponses.forEach(inv -> log.info("Inventory updated for product: {}",
                                                inv.getProductId()));
                        } catch (Exception ex) {
                                log.error("Failed to update inventory: {}", ex.getMessage(), ex);
                                // Optionally: retry, save failed inventory in DB/queue, or return partial
                                // success
                        }

                        return CompletableFuture.completedFuture(response);

                } catch (Exception e) {
                        log.error("Error in async product creation: {}", e.getMessage(), e);
                        // Optionally rethrow a custom async error wrapper or return a failed future
                        return CompletableFuture.failedFuture(e);
                }
        }
        
        */
        
        @Async
        @Override
        public CompletableFuture<List<ProductResponse>> createProductsAsync(List<ProductRequest> requests) {
            try {
                List<Product> products = requests.stream()
                        .map(req -> Product.builder()
                                .name(req.name())
                                .productId(generateUniqueProductId())
                                .description(req.description())
                                .price(req.price())
                                .build())
                        .toList();

                List<Product> saved = productRepository.saveAll(products);
                log.info("✅ Saved {} products in product table", saved.size());

                List<ProductResponse> response = saved.stream()
                        .map(this::mapToResponse)
                        .toList();

                List<InventoryRequest> inventoryRequests = response.stream()
                        .map(res -> new InventoryRequest(res.productId(), 1))
                        .toList();

                kafkaProducerService.sendToInventory(inventoryRequests);
                log.info("📤 Sent inventory creation event to Kafka for {} products", inventoryRequests.size());

                return CompletableFuture.completedFuture(response);

            } catch (Exception e) {
                log.error("❌ Error while creating products: {}", e.getMessage(), e);
                return CompletableFuture.failedFuture(e);
            }
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
                                product.getPrice()
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
        
        public void rollbackFailedInventoryRequests(List<String> failedProductCodes) {
            if (failedProductCodes == null || failedProductCodes.isEmpty()) {
                log.warn("⚠️ No failed product codes provided for rollback.");
                return;
            }

            List<Product> toDelete = productRepository.findAllByProductIdIn(failedProductCodes);
            productRepository.deleteAll(toDelete);
            log.info("🗑️ Rolled back {} products due to inventory failure", toDelete.size());
        }

}
