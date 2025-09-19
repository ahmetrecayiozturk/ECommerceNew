package com.ecommerce.productservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "outbox_product_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long productId;
    private String aggregateType;
    private String eventType;
    private boolean success;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private ZonedDateTime timestamp;
    private boolean published = false;
}
