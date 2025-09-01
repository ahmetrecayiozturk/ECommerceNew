package com.ecommerce.paymentservice.api.controller;

import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.application.PaymentApplicationService;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;
    private final PaymentRepository paymentRepository;
    private final Environment environment;

    public PaymentController(PaymentApplicationService paymentApplicationService, PaymentRepository paymentRepository, Environment environment) {
        this.paymentApplicationService = paymentApplicationService;
        this.paymentRepository = paymentRepository;
        this.environment = environment;
    }

    @PostMapping("/pay")
    public ResponseEntity<String> createPayment(@RequestBody PaymentCreateRequest paymentCreateRequest) {
            // Yeni payment oluşturuluyor
            Payment payment = new Payment();
            payment.setOrderId(paymentCreateRequest.getOrderId());
            payment.setUserId(paymentCreateRequest.getUserId());
            payment.setProductId(Long.valueOf(paymentCreateRequest.getProductId()));
            payment.setStatus(Status.COMPLETED);
            payment.setSuccess(true);
            payment.setIsPaid(true);
            payment.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            paymentRepository.save(payment);
            //aymentApplicationService.publishPaymentEvent(payment);
            return ResponseEntity.ok("Payment added and event published successfully.");
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
        return ResponseEntity.ok((List<Payment>) paymentApplicationService.getAllPayment());
    }

}
