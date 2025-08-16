package com.ecommerce.paymentservice.api;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class PaymentUpdateRequest {
    private long paymentId;
    private Long orderId;
    private Long userId;
    private String status;
    private boolean success;
    private ZonedDateTime createdAt;
}
