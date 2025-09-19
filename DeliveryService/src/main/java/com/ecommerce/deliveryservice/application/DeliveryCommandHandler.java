package com.ecommerce.deliveryservice.application;

import com.ecommerce.deliveryservice.application.service.DeliveryApplicationService;
import com.ecommerce.deliveryservice.domain.aggregate.Delivery;
import com.ecommerce.deliveryservice.domain.events.CompensationEvent;
import com.ecommerce.deliveryservice.domain.events.DeliveryEvent;
import com.ecommerce.deliveryservice.domain.model.Status;
import com.ecommerce.deliveryservice.domain.events.ProductEvent;
import com.ecommerce.deliveryservice.infrastructure.outbox.OutboxEvent;
import com.ecommerce.deliveryservice.infrastructure.outbox.OutboxRepository;
import com.ecommerce.deliveryservice.infrastructure.repository.DeliveryRepository;
import com.ecommerce.deliveryservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class DeliveryCommandHandler {
    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final DeliveryApplicationService deliveryApplicationService;
    private final JwtUtil jwtUtil;
    public DeliveryCommandHandler(DeliveryRepository deliveryRepository, KafkaTemplate<String, String> kafkaTemplate, OutboxRepository outboxRepository, ObjectMapper objectMapper, DeliveryApplicationService deliveryApplicationService, JwtUtil jwtUtil) {
        this.deliveryRepository = deliveryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.deliveryApplicationService = deliveryApplicationService;
        this.jwtUtil = jwtUtil;
    }
    @KafkaListener(topics = "delivery-commands", groupId = "payment-service")
    private void handlePaymentCommand(String payload){
        try{
            System.out.println("DeliveryCommandHandler: handlePaymentCommand");
            //burada tokeni alamıyormuşuz çünkü bu token ancak http isteği altında çalışabiliyormuş
            //String token = SecurityContextHolder.getContext().toString();
            //Long userId = (Long) jwtUtil.extractUserId(token);

            //Önce gelen eventi deserialize edelim
            ProductEvent productEvent = objectMapper.readValue(payload, ProductEvent.class);
            //Şimdi de bu eventteki paymentId ile paymenti bulup onu güncelleyelim ya da yeni bir payment oluşturalım
            Delivery delivery = deliveryRepository.findById(productEvent.getOrderId()).orElseGet(
                () -> {
                    Delivery newDelivery = new Delivery();
                    newDelivery.setOrderId(productEvent.getOrderId());
                    newDelivery.setUserId(productEvent.getUserId());
                    newDelivery.setSuccess(false);
                    newDelivery.setStatus(Status.valueOf("PENDING"));
                    newDelivery.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
                    return deliveryRepository.save(newDelivery);
                }
            );
            //Şimdi de bunu ödeme işlemini simüle ettiğimiz bir methodi ile işleyelim
            boolean isDelivered = deliveryApplicationService.isDelivered(delivery);
            if(isDelivered){
                delivery.setSuccess(true);
                delivery.setStatus(Status.valueOf("DELIVERED"));
                deliveryRepository.save(delivery);
            } else {
                delivery.setSuccess(false);
                delivery.setStatus(Status.valueOf("FAILED"));
                deliveryRepository.save(delivery);
            }
            //eventimizi yayınlalayım, şimdi bu nedir, bu payment-event topiciğine sahip bir şey olacak, zaten outbox'da da böyle tutulacak table adımız outbox_payment_events
            DeliveryEvent deliveryEvent = new DeliveryEvent();
            deliveryEvent.setOrderId(delivery.getOrderId());
            deliveryEvent.setUserId(delivery.getUserId());
            deliveryEvent.setSuccess(delivery.getSuccess());
            deliveryEvent.setStatus(String.valueOf(delivery.getStatus()));
            deliveryEvent.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")));
            //Şimdi bu eventimizi outbox'umuza atalım, önce payloadı alalım
            String payloadToOutbox = objectMapper.writeValueAsString(deliveryEvent);
            //Şimdi outbox'a ekleyelim
            OutboxEvent outboxEvent = new OutboxEvent(
                    deliveryEvent.getOrderId(),
                    deliveryEvent.getUserId(),
                    "DELIVER",
                    "DELIVERY_EVENT",
                    deliveryEvent.isSuccess(),
                    payloadToOutbox,
                    String.valueOf(deliveryEvent.getStatus())
                    );
            System.out.println("OutboxEvent: " + outboxEvent);
            //Şimdi de bunu repoya kaydedelim
            outboxRepository.save(outboxEvent);
        }
        catch(Exception e){
            throw new RuntimeException("Error handling payment command: " + e.getMessage(), e);
        }
    }
    //Şimdi de companse eventi handle edelim ve deliveryi nesnesi ile delivery event nesnesini güncelleyelim
    @KafkaListener(topics = "compensation-commands", groupId = "delivery-service")
    public void compensaDelivery (String payload){
        try{
            System.out.println("DeliveryCommandHandler: handleCompanationCommand");
            CompensationEvent compensationEvent = objectMapper.readValue(payload, CompensationEvent.class);
            Delivery delivery = deliveryRepository.findByOrderId(compensationEvent.getOrderId())
                    .orElse(null); // <--- burada orElse(null) yapıyoruz!
            if(delivery == null){
                System.out.println("Delivery not found for orderId: " + compensationEvent.getOrderId() + " (Probably already compensated or never created!)");
                return;
            }
            if(delivery.getStatus() == Status.valueOf("CANCELLED")) {
                System.out.println("Delivery already canceled for orderId: " + delivery.getOrderId());
                return;
            }
            delivery.setSuccess(false);
            delivery.setStatus(Status.valueOf("CANCELLED"));
            deliveryRepository.save(delivery);
            System.out.println("Delivery canceled for orderId: " + delivery.getOrderId());
        }
        catch(Exception e){
            throw new RuntimeException("Error handling compensation command: " + e.getMessage(), e);
        }
    }
}
