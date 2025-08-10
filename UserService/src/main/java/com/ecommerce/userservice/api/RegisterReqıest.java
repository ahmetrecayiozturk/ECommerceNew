package com.ecommerce.userservice.api;

import lombok.Data;

@Data
public class RegisterReqıest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    // No-args constructor for serialization/deserialization
    public RegisterReqıest() {
    }

    // All-args constructor for convenience
    public RegisterReqıest(String firstName, String lastName, String email, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
