package com.ecommerce.paymentservice.api.controller;

import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.application.PaymentApplicationService;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody PaymentCreateRequest paymentCreateRequest) throws IOException {
            paymentApplicationService.createPayment(paymentCreateRequest);
            return ResponseEntity.ok("Product added successfully");
    }

    /*
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateProduct(@RequestBody PaymentUpdateRequest paymentUpdateRequest) throws IOException {
        paymentApplicationService.updateProduct(paymentUpdateRequest);
        return ResponseEntity.ok("Product updated successfully");
    }*/

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/get-all")
    public ResponseEntity<List<Payment>> getAllProducts() {
        return ResponseEntity.ok((List<Payment>) paymentApplicationService.getAllPayments());
    }

}
