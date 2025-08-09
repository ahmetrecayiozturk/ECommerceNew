package com.ecommerce.userservice.infrastructure.repository;

import com.ecommerce.userservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
