package com.ecommerce.sagaservice.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "outbox_saga_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private String aggregateType;
    private String eventType;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private ZonedDateTime timestamp;
    private boolean published = false;
}
