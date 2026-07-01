package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.RegistrationRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.dto.response.RegistrationListResponse;
import com.eventregistration.event_registration_system.dto.response.RegistrationResponse;
import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.service.RegistrationService;
import com.eventregistration.event_registration_system.util.JsonConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;
    private final JsonConverter jsonConverter;

    @PostMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerAttendee(@PathVariable Long eventId,
                                                                              @Valid @RequestBody RegistrationRequest request) {
        Registration registration = registrationService.registerAttendee(eventId, request.getFormData());
        
        RegistrationResponse response = new RegistrationResponse();
        response.setId(registration.getId());
        response.setRegistrationId(registration.getRegistrationId());
        response.setQrCode(registration.getQrCode());
        response.setCheckInStatus(registration.getCheckInStatus());
        response.setCheckedInAt(registration.getCheckedInAt());
        response.setCreatedAt(registration.getCreatedAt());
        response.setFormData(jsonConverter.fromJsonToMap(registration.getFormData()));
        response.setMessage("Registration successful! Your ID is: " + registration.getRegistrationId());
        
        log.info("New registration: {} for event: {}", registration.getRegistrationId(), eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Registration successful"));
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<ApiResponse<RegistrationResponse>> getRegistration(@PathVariable String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        
        RegistrationResponse response = new RegistrationResponse();
        response.setId(registration.getId());
        response.setRegistrationId(registration.getRegistrationId());
        response.setQrCode(registration.getQrCode());
        response.setCheckInStatus(registration.getCheckInStatus());
        response.setCheckedInAt(registration.getCheckedInAt());
        response.setCreatedAt(registration.getCreatedAt());
        response.setFormData(jsonConverter.fromJsonToMap(registration.getFormData()));
        
        return ResponseEntity.ok(ApiResponse.success(response, "Registration fetched successfully"));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<RegistrationListResponse>>> getRegistrationsByEvent(@PathVariable Long eventId) {
        List<RegistrationListResponse> registrations = registrationService.getRegistrationsByEventAsDTO(eventId);
        return ResponseEntity.ok(ApiResponse.success(registrations, "Registrations fetched successfully"));
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRegistrationCount(@PathVariable Long eventId) {
        long count = registrationService.getRegistrationCount(eventId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count), "Count fetched successfully"));
    }

    @GetMapping("/event/{eventId}/checked-in")
    public ResponseEntity<ApiResponse<List<RegistrationListResponse>>> getCheckedInAttendees(@PathVariable Long eventId) {
        List<RegistrationListResponse> registrations = registrationService.getCheckedInAttendeesAsDTO(eventId);
        int count = registrations.size();
        return ResponseEntity.ok(ApiResponse.success(
            registrations, 
            count + " Checked-in attendees fetched successfully"
        ));
    }

    @GetMapping("/event/{eventId}/pending")
    public ResponseEntity<ApiResponse<List<RegistrationListResponse>>> getPendingCheckInAttendees(@PathVariable Long eventId) {
        List<RegistrationListResponse> registrations = registrationService.getPendingCheckInAttendeesAsDTO(eventId);
        int count = registrations.size();
        return ResponseEntity.ok(ApiResponse.success(
            registrations, 
            count + " Pending attendees fetched successfully"
        ));
    }

    @PutMapping("/{registrationId}/checkin")
    public ResponseEntity<ApiResponse<Registration>> checkInAttendee(@PathVariable String registrationId) {
        Registration registration = registrationService.checkInAttendee(registrationId);
        log.info("Attendee checked in: {}", registrationId);
        return ResponseEntity.ok(ApiResponse.success(registration, "Checked in successfully"));
    }

    @PutMapping("/{id}/checkin-by-id")
    public ResponseEntity<ApiResponse<Registration>> checkInAttendeeById(@PathVariable Long id) {
        Registration registration = registrationService.checkInAttendeeByLongId(id);
        log.info("Attendee checked in by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(registration, "Checked in successfully"));
    }

    // NEW: Regenerate QR Code for a registration
    @PostMapping("/{registrationId}/regenerate-qr")
    public ResponseEntity<ApiResponse<Registration>> regenerateQRCode(@PathVariable String registrationId) {
        Registration registration = registrationService.regenerateQRCode(registrationId);
        log.info("QR Code regenerated for registration: {}", registrationId);
        return ResponseEntity.ok(ApiResponse.success(registration, "QR Code regenerated successfully"));
    }
}