package com.microservice.product.service.kafka;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.microservice.product.service.service.ProductService;
import com.microservice.product.service.service.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaConsumerService {
	
    @Autowired
    private ProductService productService;


	@SuppressWarnings("unchecked")
	@KafkaListener(topics = KafkaTopics.PRODUCT_CREATION_IN_INVENTORY_RESPONSE, groupId = "product-service-group")
    public void handleInventoryStatus(Map<String, Object> response) {
        String status = (String) response.get("status");
        Object data = response.get("data");

        if ("FAIL".equals(status)) {
            log.warn("⚠️ Inventory service failed, rolling back all products");
            productService.rollbackFailedInventoryRequests((List<String>) data);
        } else if ("PARTIAL_SUCCESS".equals(status)) {
            log.warn("⚠️ Partial inventory update, rolling back failed products");
            productService.rollbackFailedInventoryRequests((List<String>) data);
        } else {
            log.info("✅ Inventory update success, no rollback needed");
        }
    }
}
