package com.ecommerce.deliveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeliveryServiceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryServiceServiceApplication.class, args);
	}

}
