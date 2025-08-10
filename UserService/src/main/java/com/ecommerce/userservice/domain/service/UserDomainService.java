package com.ecommerce.userservice.domain.service;

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




