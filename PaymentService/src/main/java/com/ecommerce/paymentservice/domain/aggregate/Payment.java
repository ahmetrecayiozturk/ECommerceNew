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
    @NotNull
    private Status status;
    @NotNull
    private boolean isPaid;
    @NotNull
    private boolean success;
    private ZonedDateTime createdAt;
    public Payment() {}
    public Payment(String status, Long orderId, boolean success,boolean isPaid) {
        this.status = Status.valueOf(status);
        this.orderId = orderId;
        this.success = success;
        this.isPaid = isPaid;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public boolean getSuccess() {
        return success;
    }
    public boolean getIsPaid() {
        return isPaid;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
}
