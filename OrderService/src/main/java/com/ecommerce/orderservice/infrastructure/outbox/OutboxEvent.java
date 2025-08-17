package com.ecommerce.orderservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "outbox_order_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private Long userId;
    private int quantity;
    private String aggregateType;
    private String eventType;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private ZonedDateTime timestamp;
    private boolean published = false;
}
