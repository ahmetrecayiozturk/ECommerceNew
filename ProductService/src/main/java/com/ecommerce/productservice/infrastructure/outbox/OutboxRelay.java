package com.ecommerce.productservice.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public OutboxRelay(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 3000)
    public void publishEvents() {
        System.out.println("OutboxRelay: publishEvents");
        List<OutboxEvent> events = outboxRepository.findByPublishedFalse();
        for (OutboxEvent event : events) {
            try {
                String payload = objectMapper.writeValueAsString(event);
                kafkaTemplate.send("product-events", payload);
                event.setPublished(true);
                outboxRepository.save(event);
                System.out.println("Published event: " + event.getId() + " to topic: " + resolveTopic(event.getEventType()));
            } catch (Exception e) {
                // Log the error and continue processing other events
                System.err.println("Error publishing event: " + e.getMessage());
                continue;
            }
        }
    }

    private String resolveTopic(String eventType) {
        // örnek: eventType’a göre topic dönüyor
        switch (eventType) {
            case "ORDER_CREATED": return "order-events";
            case "PAYMENT_COMPLETED": return "payment-events";
            case "PRODUCT_RESERVED": return "product-events";
            case "DELIVERY_COMPLETED": return "delivery-events";
            // compensation ve diğer eventler için ayrı topicler de dönebilir
            default: return "unknown-events";
        }
    }
}
