package com.ecommerce.userservice.application;


import com.ecommerce.userservice.domain.model.User;
import com.ecommerce.userservice.infrastructure.events.UserCreatedEvent;
import com.ecommerce.userservice.infrastructure.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class UserApplicationService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String firstName, String lastName, String email, String password) throws IOException {
        boolean isUserExist = existsUser(email);
        if (isUserExist) {
            throw new IllegalArgumentException("User with this email already exists.");
        } else {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password)); // Şifre hashli!
            userRepository.save(user);
            UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
            userCreatedEvent.setUserId(user.getId());
            userCreatedEvent.setFirstName(user.getFirstName());
            userCreatedEvent.setLastName(user.getLastName());
            userCreatedEvent.setEmail(user.getEmail());
            userCreatedEvent.setRole(user.getRole().name());
            userCreatedEvent.setCreatedAt(user.getCreatedAt());
            String payload = objectMapper.writeValueAsString(userCreatedEvent);
            kafkaTemplate.send("user-created-topic", payload);
            System.out.println("User registered and event sent to Kafka: " + payload);
        }
    }

    public boolean loginUser(String email, String password) {
        User user = getUserByEmail(email);
        if(user != null && passwordEncoder.matches(password, user.getPassword())){
            return true;
        }
        else{
            return false;
        }
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public boolean existsUser(String email) {
        if(getUserByEmail(email) == null){
            return false;
        };
        return true;
    }

}
