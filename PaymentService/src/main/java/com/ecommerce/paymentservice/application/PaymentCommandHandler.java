package com.ecommerce.paymentservice.application;

import com.ecommerce.paymentservice.domain.aggregate.Payment;
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
    public void handlePaymentCommand(String payload){
        try{
            OutboxEvent outboxEventTemp = objectMapper.readValue(payload, OutboxEvent.class);
            System.out.println("Gelen OutboxEvent: " + payload);
            System.out.println("OutboxEvent payload alanı: " + outboxEventTemp.getPayload());

            OrderEvent orderEvent = objectMapper.readValue(outboxEventTemp.getPayload(), OrderEvent.class);
            System.out.println("Çözümlenen OrderEvent: " + orderEvent);

            if(orderEvent.getOrderId() == null) {
                throw new RuntimeException("OrderEvent.orderId is NULL! Event: " + outboxEventTemp.getPayload());
            }
            if(orderEvent.getProductId() == null) {
                throw new RuntimeException("OrderEvent.productId is NULL! Event: " + outboxEventTemp.getPayload());
            }

            //Şimdi de bu eventteki paymentId ile paymenti bulup onu güncelleyelim ya da yeni bir payment oluşturalım
            Payment payment = paymentRepository.findById(orderEvent.getOrderId()).orElseGet(
                () -> {
                    Payment newPayment = new Payment();
                    newPayment.setOrderId(orderEvent.getOrderId());
                    newPayment.setUserId(orderEvent.getUserId());
                    newPayment.setProductId(orderEvent.getProductId());
                    newPayment.setSuccess(false);
                    newPayment.setStatus(Status.valueOf("PENDING"));
                    newPayment.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
                    return paymentRepository.save(newPayment);
                }
            );
            //Şimdi de bunu ödeme işlemini simüle ettiğimiz bir methodi ile işleyelim
            boolean isPaid = paymentApplicationService.processPayment(payment);
            if(isPaid){
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
        }
        catch(Exception e){
            throw new RuntimeException("Error handling payment command: " + e.getMessage(), e);
        }
    }
}
