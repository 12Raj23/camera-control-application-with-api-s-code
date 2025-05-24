package com.example.buttonapp.auth;

import com.example.buttonapp.dto.LoginRequest;
import com.example.buttonapp.dto.RegisterRequest;
import com.example.buttonapp.model.User;
import com.example.buttonapp.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Register attempt for username: {}", registerRequest.getUsername());

        if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty() ||
            registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty() ||
            registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Username, password, and email must be provided");
        }

        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(409).body("Username already exists");
        }

        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(409).body("Email already in use");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(registerRequest.getPassword()); // In real apps, encode the password!
        newUser.setEmail(registerRequest.getEmail());

        userService.saveUser(newUser);
        logger.info("User registered successfully: {}", registerRequest.getUsername());
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for username: {}", loginRequest.getUsername());

        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty() ||
            loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password must be provided");
        }

        boolean isAuthenticated = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (isAuthenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
