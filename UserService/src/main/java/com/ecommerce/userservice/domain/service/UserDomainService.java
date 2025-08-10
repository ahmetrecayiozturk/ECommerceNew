package com.ecommerce.userservice.domain.service;

import org.springframework.stereotype.Component;

@Component
public class UserDomainService {
    public boolean isValidEmail(String email) {
        if(email != null && email.contains("@")) {
            return true;
        }
        else{
            return false;
        }
    }
}




