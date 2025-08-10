package com.ecommerce.productservice.domain.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public enum Category {
    ELECTRONICS,
    FASHION,
    HOME_APPLIANCES,
    BOOKS,
    TOYS,
    SPORTS,
    BEAUTY;
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