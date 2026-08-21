package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.OnSpotRegistrationRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.service.AsyncEmailService;
import com.eventregistration.event_registration_system.service.SimpleBadgeService;
import com.eventregistration.event_registration_system.service.SimpleRegistrationService;
import com.eventregistration.event_registration_system.util.JsonConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simple/onspot")
@RequiredArgsConstructor
@Slf4j
public class SimpleOnSpotController {

    private final SimpleRegistrationService registrationService;
    private final SimpleBadgeService badgeService;
    private final AsyncEmailService asyncEmailService;  // Changed from EmailService to AsyncEmailService
    private final JsonConverter jsonConverter;

    @PostMapping(value = "/register/{formId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> onSpotRegisterAndPrint(
            @PathVariable Long formId,
            @Valid @RequestBody OnSpotRegistrationRequest request,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("On-spot registration by admin: {}", adminEmail);
        
        // 1. Register the user
        SimpleRegistration registration = registrationService.registerUser(formId, request.getFormData());
        log.info("User registered on-spot: {}", registration.getRegistrationId());
        
        // ===== SEND EMAIL ASYNCHRONOUSLY =====
        try {
            asyncEmailService.sendEmailAsync(registration.getRegistrationId());
            log.info("Email queued for on-spot registration: {}", registration.getRegistrationId());
        } catch (Exception e) {
            log.error("Error queuing email for on-spot registration {}: {}", 
                     registration.getRegistrationId(), e.getMessage());
        }
        
        // 2. Generate badge HTML with selected fields
        String html = badgeService.generateBadgeHTML(registration, request.getBadgeFields());
        
        // 3. Return HTML directly - ready to print
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/register-json/{formId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> onSpotRegisterJson(
            @PathVariable Long formId,
            @Valid @RequestBody OnSpotRegistrationRequest request,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("On-spot registration by admin: {}", adminEmail);
        
        // 1. Register the user
        SimpleRegistration registration = registrationService.registerUser(formId, request.getFormData());
        log.info("User registered on-spot: {}", registration.getRegistrationId());
        
        // ===== SEND EMAIL ASYNCHRONOUSLY =====
        try {
            asyncEmailService.sendEmailAsync(registration.getRegistrationId());
            log.info("Email queued for on-spot registration: {}", registration.getRegistrationId());
        } catch (Exception e) {
            log.error("Error queuing email for on-spot registration {}: {}", 
                     registration.getRegistrationId(), e.getMessage());
        }
        
        // 2. Generate badge HTML
        String html = badgeService.generateBadgeHTML(registration, request.getBadgeFields());
        
        // 3. Return JSON with registration details and HTML
        Map<String, Object> response = Map.of(
            "registrationId", registration.getRegistrationId(),
            "html", html
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "On-spot registration successful! Badge ready to print."));
    }
}