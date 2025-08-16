package com.ecommerce.paymentservice.domain.aggregate;

import com.ecommerce.paymentservice.domain.model.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "e_commerce_payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long orderId;
    @NotNull
    private Long userId;
    @NotNull
    private Long productId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
    @NotNull
    private boolean success;
    private ZonedDateTime createdAt;
    public Payment() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public Payment(String status, Long orderId, boolean success) {
        this.status = Status.valueOf(status);
        this.orderId = orderId;
        this.success = success;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public boolean getSuccess() {
        return success;
    }
}
