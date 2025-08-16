package com.ecommerce.paymentservice.application;


import com.ecommerce.paymentservice.api.PaymentCreateRequest;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.domain.events.PaymentEvent;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import com.ecommerce.paymentservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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

    public PaymentApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, PaymentRepository paymentRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void createPayment(PaymentCreateRequest paymentCreateRequest) throws IOException {
        try {
            Payment payment = new Payment();
            payment.setOrderId(paymentCreateRequest.getOrderId());
            payment.setUserId(paymentCreateRequest.getUserId());
            payment.setStatus(Status.PENDING);
            payment.setSuccess(Boolean.FALSE);
            payment.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            paymentRepository.save(payment);
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(payment.getOrderId());
            paymentEvent.setUserId(payment.getUserId());
            paymentEvent.setStatus(String.valueOf(payment.getStatus()));
            paymentEvent.setSuccess(payment.getSuccess());

//            String payload = objectMapper.writeValueAsString(paymentEvent);
//            kafkaTemplate.send("payment-created-topic", payload);
//            System.out.println("Payment process started and event sent to Kafka: " + payload);
        }
        catch (Exception e) {
            System.err.println("Error while payment process : " + e.getMessage());
            throw new RuntimeException("Payment process failed", e);
        }
    }
    public boolean processPayment(Payment payment) {
        // Banka veya ödeme servisine istek atılır
        // Burada random veya fixed true/false dönebilirsin
        return true; // Ödeme başarılı
    }
    /*
    @Transactional
    public void updateProduct(PaymentUpdateRequest paymentUpdateRequest) {
        try{
            Payment payment = paymentRepository.findById(paymentUpdateRequest.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            payment.setStatus(Status.valueOf(paymentUpdateRequest.getStatus()));
            payment.setSuccess(paymentUpdateRequest.getSuccess());
            payment.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            // Eğer ödeme başarılı ise, ödeme işlemi tamamlandı olarak işaretle
            if (paymentUpdateRequest.getSuccess()) {
                payment.setStatus(Status.COMPLETED);
            } else {
                payment.setStatus(Status.FAILED);
            }
            paymentRepository.save(payment);
            PaymentEvent productUpdatedEvent = new PaymentEvent(payment.getOrderId(), payment.getUserId(), payment.getStatus(), payment.getSuccess());
            productUpdatedEvent.setProductId(payment.getId());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }*/

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

    public Object getAllPayments() {
        try {
            return paymentRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error while retrieving products: " + e.getMessage());
            throw new RuntimeException("Product retrieval failed", e);
        }
    }

}
