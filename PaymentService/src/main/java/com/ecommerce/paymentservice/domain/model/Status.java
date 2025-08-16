package com.ecommerce.paymentservice.domain.model;

public enum Status {
    PENDING,
    COMPLETED,
    FAILED,
    SUCCESS,
    REFUNDED;

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