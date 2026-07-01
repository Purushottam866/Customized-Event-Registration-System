package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.SimpleRegistrationRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.dto.response.SimpleRegistrationResponse;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.service.SimpleRegistrationService;
import com.eventregistration.event_registration_system.util.JsonConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/simple/registrations")
@RequiredArgsConstructor
@Slf4j
public class SimpleRegistrationController {

    private final SimpleRegistrationService registrationService;
    private final JsonConverter jsonConverter;

    @PostMapping("/form/{formId}")
    public ResponseEntity<ApiResponse<SimpleRegistrationResponse>> registerUser(
            @PathVariable Long formId,
            @Valid @RequestBody SimpleRegistrationRequest request) {
        
        SimpleRegistration registration = registrationService.registerUser(formId, request.getFormData());
        
        SimpleRegistrationResponse response = SimpleRegistrationResponse.builder()
                .id(registration.getId())
                .registrationId(registration.getRegistrationId())
                .qrCode(registration.getQrCode())
                .createdAt(registration.getCreatedAt())
                .formData(jsonConverter.fromJsonToMap(registration.getFormData()))
                .message("Registration successful! Your ID is: " + registration.getRegistrationId())
                .build();
        
        log.info("New simple registration: {}", registration.getRegistrationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Registration successful"));
    }

    @GetMapping("/form/{formId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegistrationsByForm(@PathVariable Long formId) {
        List<SimpleRegistration> registrations = registrationService.getRegistrationsByForm(formId);
        
        List<SimpleRegistrationResponse> responses = registrations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", responses.size());
        result.put("registrations", responses);
        
        String message = responses.size() + " Registrations fetched successfully";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @GetMapping("/form/{formId}/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchRegistrations(
            @PathVariable Long formId,
            @RequestParam(required = false) String searchTerm) {
        
        List<SimpleRegistration> registrations = registrationService.searchRegistrations(formId, searchTerm);
        
        List<SimpleRegistrationResponse> responses = registrations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", responses.size());
        result.put("registrations", responses);
        
        String message = responses.size() + " Registrations found";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @GetMapping("/form/{formId}/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRegistrationCount(@PathVariable Long formId) {
        long count = registrationService.getRegistrationCount(formId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count), "Count fetched successfully"));
    }

    private SimpleRegistrationResponse convertToResponse(SimpleRegistration registration) {
        return SimpleRegistrationResponse.builder()
                .id(registration.getId())
                .registrationId(registration.getRegistrationId())
                .formData(jsonConverter.fromJsonToMap(registration.getFormData()))
                .qrCode(registration.getQrCode())
                .createdAt(registration.getCreatedAt())
                .build();
    }
}