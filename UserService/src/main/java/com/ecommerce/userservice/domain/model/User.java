package com.ecommerce.userservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "e_commerce_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String password;
    @NonNull
    private String email;
    @Column(nullable = true)
    private String phone;
    @Column(nullable = true)
    private String address;
    @Column(nullable = true)
    private String city;
    @Column(nullable = true)
    private String country = "Türkiye";
    @Column(nullable = true)
    private String zipCode;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; // e.g., "USER", "ADMIN"
    private boolean isActive;
    private ZonedDateTime createdAt;

    public User() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
    }
}
