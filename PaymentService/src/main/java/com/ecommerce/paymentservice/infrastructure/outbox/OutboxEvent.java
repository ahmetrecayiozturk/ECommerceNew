package com.ecommerce.paymentservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "outbox_payment_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long productId;
    private String aggregateType;
    private String eventType;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private ZonedDateTime timestamp;
    private boolean success;
    private boolean published = false;
    private String paymentStatus; // e.g., "PENDING", "COMPLETED", "FAILED"
    public OutboxEvent(boolean success, Long orderId, Long userId, Long productId, String aggregateType, String eventType, String payload, String paymentStatus) {
        this.success = success;
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
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
