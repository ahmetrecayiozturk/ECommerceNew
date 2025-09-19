package com.ecommerce.paymentservice.application.service;

import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxRepository;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import com.ecommerce.paymentservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PaymentApplicationService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OutboxRepository outboxRepository;

    public PaymentApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, PaymentRepository paymentRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, OutboxRepository outboxRepository) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.outboxRepository = outboxRepository;
    }

    public Payment createPayment(PaymentCreateRequest paymentCreateRequest) {
        try {
            Payment payment = new Payment();
            payment.setOrderId(paymentCreateRequest.getOrderId());
            payment.setUserId(paymentCreateRequest.getUserId());
            payment.setProductId(Long.valueOf(paymentCreateRequest.getProductId()));
            payment.setStatus(Status.COMPLETED);
            payment.setSuccess(true);
            payment.setIsPaid(true);
            payment.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            paymentRepository.save(payment);
            return payment;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating payment: " + e.getMessage(), e);
        }
    }

    public List<Payment> getAllPayment(){
        try{
            List<Payment> payments = paymentRepository.findAll();
            if(payments.isEmpty()){
                throw new RuntimeException("No payments found");
            }
            return payments;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean processPayment(Payment payment) {
        Payment payment_ = paymentRepository.findByOrderId(payment.getOrderId()).orElse(null);
        if(payment_ == null){
            System.out.println("Payment not found for orderId: " + payment.getOrderId());
            return false;
        }
        else if(payment_.getIsPaid()){
            System.out.println("Payment already completed for orderId: " + payment.getOrderId());
            return true;
        }
        else{
            System.out.println("Payment cannot be completed for some reason: " + payment.getOrderId());
            return false;
        }
    }
}

/*package com.ecommerce.paymentservice.application;

import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.domain.events.PaymentEvent;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxRepository;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import com.ecommerce.paymentservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PaymentApplicationService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OutboxRepository outboxRepository;

    public PaymentApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, PaymentRepository paymentRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, OutboxRepository outboxRepository) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.outboxRepository = outboxRepository;
    }

    public Payment createPayment(PaymentCreateRequest paymentCreateRequest) {
        try {
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
            //publishPaymentEvent(payment);
            return payment;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating payment: " + e.getMessage(), e);
        }
    }
    ---
    @Transactional
    public void publishPaymentEvent(Payment payment) {
        try {
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(payment.getOrderId());
            paymentEvent.setUserId(payment.getUserId());
            paymentEvent.setProductId(payment.getProductId());
            paymentEvent.setStatus(String.valueOf(payment.getStatus()));
            paymentEvent.setSuccess(payment.getSuccess());
            paymentEvent.setCreatedAt(payment.getCreatedAt());

            String payload = objectMapper.writeValueAsString(paymentEvent);
            OutboxEvent outboxEvent = new OutboxEvent(
                    payment.getSuccess(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    "PAYMENT",
                    "PAYMENT_EVENT",
                    payload,
                    String.valueOf(paymentEvent.getStatus())
            );
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error while publishing payment event: " + e.getMessage(), e);
        }
    }---

    public List<Payment> getAllPayment(){
        try{
            List<Payment> payments = paymentRepository.findAll();
            if(payments.isEmpty()){
                throw new RuntimeException("No products found");
            }
            return payments;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public boolean processPayment(Payment payment) {
        Payment payment_ = paymentRepository.findByOrderId(payment.getOrderId()).orElse(null);
        if(payment_ == null){
            System.out.println("Payment not found for orderId: " + payment.getOrderId());
            return false;
        }
        else if(payment_.getIsPaid()){
            System.out.println("Payment already completed for orderId: " + payment.getOrderId());
            return true;
        }
        else{
            System.out.println("Payment cannot completed for some reason: " + payment.getOrderId());
            return false;
        }
    }
}
*/