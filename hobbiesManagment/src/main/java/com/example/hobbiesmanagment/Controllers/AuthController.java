package com.example.hobbiesmanagment.Controllers;

import com.example.hobbiesmanagment.DTO.AuthResponse;
import com.example.hobbiesmanagment.DTO.LoginRequest;
import com.example.hobbiesmanagment.DTO.UserRegistrationDto;
import com.example.hobbiesmanagment.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRegistrationDto registrationDto) {
        if (registrationDto.getEmail() == null || registrationDto.getEmail().isBlank())
            throw new IllegalArgumentException("Email is a required field.");

        if (registrationDto.getPassword() == null || registrationDto.getPassword().isBlank())
            throw new IllegalArgumentException("Password is a required field.");

        AuthResponse response = userService.register(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank())
            throw new IllegalArgumentException("Email is a required field.");

        AuthResponse response = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }
}