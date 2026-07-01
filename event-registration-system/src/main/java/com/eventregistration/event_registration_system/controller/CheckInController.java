package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.dto.response.RegistrationListResponse;
import com.eventregistration.event_registration_system.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Slf4j
public class CheckInController {

    private final RegistrationService registrationService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RegistrationListResponse>>> searchRegistrations(
            @RequestParam Long eventId,
            @RequestParam(required = false) String searchTerm) {
        List<RegistrationListResponse> results = registrationService.searchRegistrationsAsDTO(eventId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success(results, "Search completed successfully"));
    }

    // ... rest of the methods remain the same
}