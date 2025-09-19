package com.ecommerce.deliveryservice.application.service;


import com.ecommerce.deliveryservice.api.DeliveryCreateRequest;
import com.ecommerce.deliveryservice.domain.aggregate.Delivery;
import com.ecommerce.deliveryservice.domain.model.Status;
import com.ecommerce.deliveryservice.infrastructure.repository.DeliveryRepository;
import com.ecommerce.deliveryservice.infrastructure.security.jwt.JwtUtil;
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
public class DeliveryApplicationService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DeliveryRepository deliveryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public DeliveryApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, DeliveryRepository deliveryRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.deliveryRepository = deliveryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void createDelivery(DeliveryCreateRequest deliveryCreateRequest) throws IOException {
        try {
            Delivery delivery = new Delivery();
            delivery.setOrderId(deliveryCreateRequest.getOrderId());
            delivery.setUserId(deliveryCreateRequest.getUserId());
            delivery.setStatus(Status.PENDING);
            delivery.setSuccess(Boolean.FALSE);
            delivery.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            deliveryRepository.save(delivery);
        }
        catch (Exception e) {
            System.err.println("Error while delivery process : " + e.getMessage());
            throw new RuntimeException("Payment process failed", e);
        }
    }
    public boolean isDelivered(Delivery delivery) {
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

    public List<Delivery> getAllPayment(){
        try{
            List<Delivery> deliveries = deliveryRepository.findAll();
            if(deliveries.isEmpty()){
                throw new RuntimeException("No delivery found");
            }
            return deliveries;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public Object getAllDelivery() {
        try {
            return deliveryRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error while retrieving delivery: " + e.getMessage());
            throw new RuntimeException("Delivery retrieval failed", e);
        }
    }

}
