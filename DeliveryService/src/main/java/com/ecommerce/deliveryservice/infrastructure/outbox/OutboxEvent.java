package com.ecommerce.deliveryservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "outbox_delivery_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private String aggregateType;
    private String eventType;
    private String payload;
    private ZonedDateTime timestamp;
    private boolean published = false;
    private String paymentStatus; // e.g., "PENDING", "COMPLETED", "FAILED"
    public OutboxEvent(Long orderId, Long userId, String aggregateType, String eventType, String payload, String paymentStatus) {
        this.orderId = orderId;
        this.userId = userId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = ZonedDateTime.now();
        this.paymentStatus = paymentStatus;
        this.published = false;

    }
    public OutboxEvent() {

    }
}
