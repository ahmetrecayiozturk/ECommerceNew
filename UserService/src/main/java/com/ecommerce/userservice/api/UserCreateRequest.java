package com.ecommerce.userservice.api;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    // No-args constructor for serialization/deserialization
    public UserCreateRequest() {
    }

    // All-args constructor for convenience
    public UserCreateRequest(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}
