package com.ecommerce.userservice.infrastructure.events;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Data
public class UserCreatedEvent {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private ZonedDateTime createdAt;
}
