package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.LoginRequest;
import com.eventregistration.event_registration_system.dto.request.RegisterUserRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.dto.response.AuthResponse;
import com.eventregistration.event_registration_system.entity.User;
import com.eventregistration.event_registration_system.service.AuthService;
import com.eventregistration.event_registration_system.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Login using email
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = authService.getUserByEmail(request.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        AuthResponse authResponse = new AuthResponse(
            token,
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name(),
            user.getId()
        );
        
        log.info("User logged in: {} with email: {}", user.getUsername(), user.getEmail());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        User user = authService.registerUser(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getFullName(),
            request.getRole()
        );
        log.info("New user registered: {} with email: {}", user.getUsername(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(user, "User registered successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = authService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user, "User details fetched successfully"));
    }
}