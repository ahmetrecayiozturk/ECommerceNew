package com.ecommerce.sagaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SagaServiceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SagaServiceServiceApplication.class, args);
	}

}
