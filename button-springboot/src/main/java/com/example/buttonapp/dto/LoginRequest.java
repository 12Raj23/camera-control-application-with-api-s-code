package com.example.buttonapp.dto;

public class LoginRequest {
    private String username;
    private String password;

    // Default constructor (required by Spring)
    public LoginRequest() {}

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
