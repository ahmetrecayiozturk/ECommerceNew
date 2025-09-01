package com.ecommerce.paymentservice.api;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class PaymentCreateRequest {
    private Long orderId;
    private String productId;
    private Long userId;
    //private String status;
    //private boolean success;
}
