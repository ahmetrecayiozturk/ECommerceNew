package com.ecommerce.paymentservice.application;

import com.ecommerce.paymentservice.application.service.PaymentApplicationService;
import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.events.CompensationEvent;
import com.ecommerce.paymentservice.domain.events.PaymentEvent;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.domain.events.OrderEvent;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxRepository;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class PaymentCommandHandler {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentApplicationService paymentApplicationService;

    public PaymentCommandHandler(PaymentRepository paymentRepository, KafkaTemplate<String, String> kafkaTemplate, OutboxRepository outboxRepository, ObjectMapper objectMapper, PaymentApplicationService paymentApplicationService) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.paymentApplicationService = paymentApplicationService;
    }

    @KafkaListener(topics = "payment-commands", groupId = "payment-service")
    public void handlePaymentCommand(String message) {
        try {
            // 1. OutboxEvent'i deserialize et
            OutboxEvent outboxEvent = objectMapper.readValue(message, OutboxEvent.class);

            // 2. payload'ı tekrar OrderEvent'e deserialize et
            OrderEvent orderEvent = objectMapper.readValue(outboxEvent.getPayload(), OrderEvent.class);

            //Logs
            System.out.println("OrderEvent: " + orderEvent);
            System.out.println("OrderEvent.productId: " + orderEvent.getProductId());
            System.out.println("OrderEvent.productSnapshot: " + orderEvent.getProductSnapshot());
            if (orderEvent.getProductSnapshot() != null)
                System.out.println("OrderEvent.productSnapshot.productId: " + orderEvent.getProductSnapshot().getProductId());


            System.out.println("Çözümlenen OrderEvent: " + orderEvent);

            if (orderEvent.getOrderId() == null) {
                System.out.println("OrderEvent.orderId is NULL! Event: " + outboxEvent.getPayload());
            }
            if (orderEvent.getProductId() == null) {
                System.out.println("OrderEvent.productId is NULL! Event: " + outboxEvent.getPayload());
            }
            if (orderEvent.getProductSnapshot() == null || orderEvent.getProductSnapshot().getName() == null) {
                System.out.println("OrderEvent.productSnapshot is NULL or name is NULL! Event: " + outboxEvent.getPayload());
            }

            Payment payment = paymentRepository.findByOrderId(orderEvent.getOrderId()).orElse(null);
            if (payment == null) {
                System.out.println("Payment bulunamadı, event publish edilmeyecek. OrderId: " + orderEvent.getOrderId());
                return;
            }

            boolean isPaid = paymentApplicationService.processPayment(payment);

            if (isPaid) {
                payment.setSuccess(true);
                payment.setStatus(Status.COMPLETED);
                paymentRepository.save(payment);
                System.out.println("Payment successful for order ID: " + payment.getOrderId());
            } else {
                payment.setSuccess(false);
                payment.setStatus(Status.FAILED);
                paymentRepository.save(payment);
                System.out.println("Payment failed for order ID: " + payment.getOrderId());
            }

            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(orderEvent.getOrderId());
            paymentEvent.setUserId(orderEvent.getUserId());


            Long productId = orderEvent.getProductId();
            if (productId == null && orderEvent.getProductSnapshot() != null) {
                productId = orderEvent.getProductSnapshot().getProductId();
            }
            paymentEvent.setProductId(productId);
            paymentEvent.setQuantity(orderEvent.getQuantity());
            paymentEvent.setSuccess(payment.getSuccess());
            paymentEvent.setStatus(String.valueOf(payment.getStatus()));
            paymentEvent.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));

            String payloadToOutbox = objectMapper.writeValueAsString(paymentEvent);

            OutboxEvent outboxEventToSave = new OutboxEvent(
                    payment.getSuccess(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getUserId(),
                    paymentEvent.getProductId(),
                    "PAYMENT",
                    "PAYMENT_EVENT",
                    payloadToOutbox,
                    String.valueOf(paymentEvent.getStatus())
            );
            outboxRepository.save(outboxEventToSave);
        } catch (Exception e) {
            throw new RuntimeException("Error handling payment command: " + e.getMessage(), e);
        }

    }

    @KafkaListener(topics = "compensation-commands", groupId = "payment-service")
    public void handleCompensationCommand(String payload) {
        try {
            CompensationEvent compensationEvent = objectMapper.readValue(payload, CompensationEvent.class);
            Payment payment = paymentRepository.findByOrderId(compensationEvent.getOrderId()).orElseThrow(
                    () -> new RuntimeException("Payment not found for orderId: " + compensationEvent.getOrderId())
            );
            if (payment.getStatus() == Status.REFUNDED) {
                System.out.println("Payment already refunded for orderId: " + payment.getOrderId());
                return;
            }
            if (!payment.getSuccess()) {
                System.out.println("Payment already failed for orderId: " + payment.getOrderId() + ", no need to refund.");
                return;
            }
            payment.setStatus(Status.REFUNDED);
            payment.setSuccess(false);
            paymentRepository.save(payment);
            System.out.println("Payment refunded for orderId: " + payment.getOrderId());
        } catch (Exception e) {
            throw new RuntimeException("Error handling compensation command: " + e.getMessage(), e);
        }
    }
}
/*
package com.ecommerce.paymentservice.application;

import com.ecommerce.paymentservice.domain.aggregate.Payment;
import com.ecommerce.paymentservice.domain.events.CompensationEvent;
import com.ecommerce.paymentservice.domain.events.PaymentEvent;
import com.ecommerce.paymentservice.domain.model.Status;
import com.ecommerce.paymentservice.domain.events.OrderEvent;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.paymentservice.infrastructure.outbox.OutboxRepository;
import com.ecommerce.paymentservice.infrastructure.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class PaymentCommandHandler {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentApplicationService paymentApplicationService;

    public PaymentCommandHandler(PaymentRepository paymentRepository, KafkaTemplate<String, String> kafkaTemplate, OutboxRepository outboxRepository, ObjectMapper objectMapper, PaymentApplicationService paymentApplicationService) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.paymentApplicationService = paymentApplicationService;
    }

    @KafkaListener(topics = "payment-commands", groupId = "payment-service")
    public void handlePaymentCommand(String payload) {
        try {
            OrderEvent orderEvent = objectMapper.readValue(payload, OrderEvent.class); // Doğru kullanım
            System.out.println("Çözümlenen OrderEvent: " + orderEvent);

            if (orderEvent.getOrderId() == null) {
                System.out.println("OrderEvent.orderId is NULL! Event: " + payload);
            }
            if (orderEvent.getProductId() == null) {
                System.out.println("OrderEvent.productId is NULL! Event: " + payload);
            }

            Payment payment = paymentRepository.findByOrderId(orderEvent.getOrderId()).orElse(null);

            if (payment == null) {
                System.out.println("Payment bulunamadı, event publish edilmeyecek. OrderId: " + orderEvent.getOrderId());
                return;
            }

            //Şimdi de bunu ödeme işlemini simüle ettiğimiz bir methodi ile işleyelim
            boolean isPaid = paymentApplicationService.processPayment(payment);

            if (isPaid) {
                payment.setSuccess(true);
                payment.setStatus(Status.valueOf("COMPLETED"));
                paymentRepository.save(payment);
                System.out.println("Payment successful for order ID: " + payment.getOrderId());
            } else {
                payment.setSuccess(false);
                payment.setStatus(Status.valueOf("FAILED"));
                paymentRepository.save(payment);
            }
            //eventimizi yayınlalayım, şimdi bu nedir, bu payment-event topiciğine sahip bir şey olacak, zaten outbox'da da böyle tutulacak table adımız outbox_payment_events
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setOrderId(orderEvent.getOrderId());
            paymentEvent.setUserId(orderEvent.getUserId());
            paymentEvent.setProductId(orderEvent.getProductId());
            paymentEvent.setQuantity(orderEvent.getQuantity());
            paymentEvent.setSuccess(payment.getSuccess());
            paymentEvent.setStatus(String.valueOf(payment.getStatus()));
            paymentEvent.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            //Şimdi bu eventimizi outbox'umuza atalım, önce payloadı alalım
            String payloadToOutbox = objectMapper.writeValueAsString(paymentEvent);
            //Şimdi outbox'a ekleyelim
            OutboxEvent outboxEvent = new OutboxEvent(
                    payment.getSuccess(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getUserId(),
                    "PAYMENT",
                    "PAYMENT_EVENT",
                    payloadToOutbox,
                    String.valueOf(paymentEvent.getStatus())
            );
            //Şimdi de bunu repoya kaydedelim
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error handling payment command: " + e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "compensation-commands", groupId = "payment-service")
    public void handleCompensationCommand(String payload) {
        try {
            //gidip compensation eventini bulalım payload ile sonra da içindekii orderId ile gidip payment'i bulalım sonra da paymenti geri alalım iade yapalım yani
            CompensationEvent compensationEvent = objectMapper.readValue(payload, CompensationEvent.class);
            //Şimdi de paymenti bulalım
            Payment payment = paymentRepository.findByOrderId(compensationEvent.getOrderId()).orElseThrow(
                    () -> new RuntimeException("Payment not found for orderId: " + compensationEvent.getOrderId())
            );
            if (payment.getStatus() == Status.REFUNDED) {
                System.out.println("Payment already refunded for orderId: " + payment.getOrderId());
                return;
            }
            if (payment.getSuccess() == false) {
                System.out.println("Payment already failed for orderId: " + payment.getOrderId() + ", no need to refund.");
                return;
            }
            payment.setStatus(Status.REFUNDED);
            payment.setSuccess(false);
            paymentRepository.save(payment);
            System.out.println("Payment refunded for orderId: " + payment.getOrderId());
        } catch (Exception e) {
            throw new RuntimeException("Error handling compensation command: " + e.getMessage(), e);
        }
    }
}*/
