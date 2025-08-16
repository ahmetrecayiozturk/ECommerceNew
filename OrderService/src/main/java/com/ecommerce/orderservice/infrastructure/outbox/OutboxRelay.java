package com.ecommerce.orderservice.infrastructure.outbox;

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

    @Scheduled(fixedDelay = 5000)
    private void publishOutboxEvents(){
        System.out.println("Publishing outbox events...");
        System.out.println(outboxRepository.findAll());
        List<OutboxEvent> outboxEvents = outboxRepository.findByPublishedFalse();
        for(OutboxEvent outboxEvent : outboxEvents){
            try{
                String payload = objectMapper.writeValueAsString(outboxEvent);
                kafkaTemplate.send("order-events", payload);
                outboxEvent.setPublished(true);
                outboxRepository.save(outboxEvent);
                System.out.println("Outbox event published successfully: " + payload);
            }
            catch (Exception e) {
                // Handle serialization error
                e.printStackTrace();
                continue;
            }
        }
    }
}
