package com.ecommerce.userservice.api;

import com.ecommerce.userservice.application.UserApplicationService;
import com.ecommerce.userservice.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userService;

    public UserController(UserApplicationService userService) {
        this.userService = userService;
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
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest){
        try{
            boolean isLoggedIn = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            if(isLoggedIn){
                return ResponseEntity.ok("User logged in successfully.");
            } else {
                return ResponseEntity.status(401).body("Invalid email or password.");
            }
        }
        catch (Exception e){
            return ResponseEntity.status(400).body("Error logging in user: " + e.getMessage());
        }
    }

    @RequestMapping("/test")
    public String test() {
        return "Test from User Service!";
    }
}
