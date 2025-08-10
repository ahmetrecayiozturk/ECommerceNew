package com.ecommerce.userservice.api;

import com.ecommerce.userservice.application.UserApplicationService;
import com.ecommerce.userservice.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.userservice.application.UserApplicationService;
import com.ecommerce.userservice.domain.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userService;
    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userService, UserApplicationService userApplicationService) {
        this.userService = userService;
        this.userApplicationService = userApplicationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserCreateRequest userCreateRequest){
        User user = new User();
        user.setFirstName(userCreateRequest.getFirstName());
        user.setLastName(userCreateRequest.getLastName());
        user.setEmail(userCreateRequest.getEmail());
        user.setPassword(userCreateRequest.getPassword()); // Password should be encoded in the service layer
        try {
            userService.registerUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
            return ResponseEntity.ok("User registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        try {
            String token = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(java.util.Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(java.util.Map.of("error", "Error logging in user: " + e.getMessage()));
        }
    }

    @GetMapping("/user-test")
    public ResponseEntity<String> userTest(HttpServletRequest request) {
        String role = userApplicationService.getRole(request);
        if (role == null) {
            return ResponseEntity.status(401).body("Not authenticated!");
        }
        if(role.equals("ROLE_USER")){
            return ResponseEntity.ok("YOu are a User i had understood");
        }
        else{
            return ResponseEntity.ok("You are NOT a User!");
        }
    }

    @GetMapping("/user-role-test")
    public ResponseEntity<String> userTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated!");
        }
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
        if (isUser) {
            return ResponseEntity.ok("You are a USER!");
        } else {
            return ResponseEntity.ok("You are NOT a USER!");
        }
    }
}
