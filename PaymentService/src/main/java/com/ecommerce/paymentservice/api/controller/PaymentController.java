package com.ecommerce.paymentservice.api.controller;

import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.application.PaymentApplicationService;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;
    private final Environment environment;

    public PaymentController(PaymentApplicationService paymentApplicationService, Environment environment) {
        this.paymentApplicationService = paymentApplicationService;
        this.environment = environment;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestBody PaymentCreateRequest paymentCreateRequest) throws IOException {
            paymentApplicationService.createPayment(paymentCreateRequest);
            return ResponseEntity.ok("Product added successfully");
    }

    /*
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateProduct(@RequestBody PaymentUpdateRequest paymentUpdateRequest) throws IOException {
        paymentApplicationService.updateProduct(paymentUpdateRequest);
        return ResponseEntity.ok("Product updated successfully");
    }*/
    @PostMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        String port = environment.getProperty("local.server.port");
        String msg = "Payment Service is up and running on port: " + port;
        System.out.println(msg);
        return ResponseEntity.ok(msg);
    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/get-all")
    public ResponseEntity<List<Payment>> getAllProducts() {
        return ResponseEntity.ok((List<Payment>) paymentApplicationService.getAllPayments());
    }

}
