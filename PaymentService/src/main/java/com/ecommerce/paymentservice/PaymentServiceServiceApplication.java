package com.ecommerce.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentServiceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceServiceApplication.class, args);
	}

}
