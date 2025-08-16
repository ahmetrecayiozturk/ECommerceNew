package com.ecommerce.orderservice.domain.aggregate;

import com.ecommerce.orderservice.domain.snapshout.ProductSnapshot;
import com.ecommerce.orderservice.domain.value.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "e_commerce_orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    private int quantity;
    @NotNull
    private ProductSnapshot productSnapshot;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @NotNull
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneId.of("UTC"));

    public void markProcessing() {
        this.status = OrderStatus.PROCESSING;
    }
    public void markPaid() {
        this.status = OrderStatus.PAID;
    }
    public void markCancelled() {
        this.status = OrderStatus.CANCELLED;
    }
    public void markCompleted() {
        this.status = OrderStatus.COMPLETED;
    }

}
