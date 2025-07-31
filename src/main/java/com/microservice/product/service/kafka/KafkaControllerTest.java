package com.microservice.product.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/kafka")
public class KafkaControllerTest {

	@Autowired
	private KafkaProducerService kafkaService;
	
	
	@PostMapping
	public void kafkaTest() {
		for(int i = 0; i < 10; i++) {
			this.kafkaService.sendToInventory(null);
		}
	}
}
