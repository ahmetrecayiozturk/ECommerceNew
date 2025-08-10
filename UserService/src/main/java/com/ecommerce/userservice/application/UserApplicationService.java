package com.ecommerce.userservice.application;


import com.ecommerce.userservice.domain.model.Role;
import com.ecommerce.userservice.domain.model.User;
import com.ecommerce.userservice.infrastructure.events.UserCreatedEvent;
import com.ecommerce.userservice.infrastructure.repository.UserRepository;
import com.ecommerce.userservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserApplicationService(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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

    public String loginUser(String email, String password) {
        User user = getUserByEmail(email);
        if(user != null && passwordEncoder.matches(password, user.getPassword())){
            if(user.getRole() == null){
                throw new IllegalArgumentException("User role is not set.");
            }
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            if(authentication.isAuthenticated()){
                String token = jwtUtil.generateToken(email);
                return token;
            }
            else{
                throw new IllegalArgumentException("Invalid email or password.");
            }
        }
        else{
            throw new IllegalArgumentException("Invalid email or password.");
        }
    }

    public String getRole(HttpServletRequest request){
        String token = jwtUtil.getTokenFromHeader(request);
        String email = jwtUtil.extractEmail(token);
        User user = getUserByEmail(email);
        if(user == null){
            throw new IllegalArgumentException("User not found.");
        }
        String role = user.getRole().name();
        return "ROLE_" + role;
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
