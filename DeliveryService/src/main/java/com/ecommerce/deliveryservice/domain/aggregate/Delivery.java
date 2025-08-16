package com.ecommerce.deliveryservice.domain.aggregate;

import com.ecommerce.deliveryservice.domain.model.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "e_commerce_delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long orderId;
    @NotNull
    private Long userId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
    @NotNull
    private boolean success = false;
    private ZonedDateTime createdAt;
    public Delivery() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public Delivery(String status, Long orderId, boolean success) {
        this.status = Status.valueOf(status);
        this.orderId = orderId;
        this.success = success;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public boolean getSuccess() {
        return success;
    }
}
