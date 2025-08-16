package com.ecommerce.deliveryservice.domain.model;

public enum Status {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    RETURNED;

    public String getStatus() {
        return this.name();
    }
}
/*
    USER,
    ADMIN,
    MODERATOR,
    GUEST;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
 */