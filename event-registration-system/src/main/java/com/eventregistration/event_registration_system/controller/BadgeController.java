package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.BadgePrintRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.entity.BadgePrint;
import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.service.BadgeService;
import com.eventregistration.event_registration_system.service.EventService;
import com.eventregistration.event_registration_system.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badge")
@RequiredArgsConstructor
@Slf4j
public class BadgeController {

    private final BadgeService badgeService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    /**
     * Generate badge using Registration ID (String) - Uses event's default badge fields
     */
    @PostMapping(value = "/generate/{registrationId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generateBadgeByRegistrationId(
            @PathVariable String registrationId,
            @RequestBody(required = false) BadgePrintRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = 1L;
        
        Registration registration = registrationService.getRegistrationById(registrationId);
        Long eventId = registration.getEvent().getId();
        
        // Get default badge fields from event
        List<String> defaultFields = eventService.getBadgeFields(eventId);
        
        // Use request fields if provided, otherwise use event defaults
        List<String> selectedFields = (request != null && request.getSelectedFields() != null && !request.getSelectedFields().isEmpty())
                ? request.getSelectedFields()
                : defaultFields;
        
        // Log the badge print
        badgeService.logEventBadgePrint(registration, selectedFields, userId);
        
        String html = badgeService.generateBadgeHTML(registrationId, selectedFields, userId);
        log.info("Badge generated for registration: {} with fields: {}", registrationId, selectedFields);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Generate badge using Long ID
     */
    @PostMapping(value = "/generate-by-id/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generateBadgeById(
            @PathVariable Long id,
            @RequestBody(required = false) BadgePrintRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = 1L;
        
        Registration registration = registrationService.getRegistrationByLongId(id);
        Long eventId = registration.getEvent().getId();
        
        // Get default badge fields from event
        List<String> defaultFields = eventService.getBadgeFields(eventId);
        
        // Use request fields if provided, otherwise use event defaults
        List<String> selectedFields = (request != null && request.getSelectedFields() != null && !request.getSelectedFields().isEmpty())
                ? request.getSelectedFields()
                : defaultFields;
        
        // Log the badge print
        badgeService.logEventBadgePrint(registration, selectedFields, userId);
        
        String html = badgeService.generateBadgeHTML(id, selectedFields, userId);
        log.info("Badge generated for registration ID: {} with fields: {}", id, selectedFields);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping(value = "/generate-bulk", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generateBulkBadges(
            @Valid @RequestBody BadgePrintRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Long userId = 1L;
        
        // Get event ID from first registration
        if (request.getRegistrationIds() == null || request.getRegistrationIds().isEmpty()) {
            throw new IllegalArgumentException("No registrations selected");
        }
        
        Registration firstRegistration = registrationService.getRegistrationByLongId(request.getRegistrationIds().get(0));
        Long eventId = firstRegistration.getEvent().getId();
        
        // Get default badge fields from event
        List<String> defaultFields = eventService.getBadgeFields(eventId);
        
        // Use request fields if provided, otherwise use event defaults
        List<String> selectedFields = (request.getSelectedFields() != null && !request.getSelectedFields().isEmpty())
                ? request.getSelectedFields()
                : defaultFields;
        
        // Log badge prints for all registrations
        for (Long regId : request.getRegistrationIds()) {
            Registration registration = registrationService.getRegistrationByLongId(regId);
            badgeService.logEventBadgePrint(registration, selectedFields, userId);
        }
        
        String html = badgeService.generateBulkBadgeHTML(request.getRegistrationIds(), 
                                                         selectedFields, 
                                                         userId);
        log.info("Bulk badges generated for {} registrations with fields: {}", 
                 request.getRegistrationIds().size(), selectedFields);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/history/{registrationId}")
    public ResponseEntity<ApiResponse<List<BadgePrint>>> getPrintHistory(@PathVariable String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        List<BadgePrint> history = badgeService.getEventPrintHistory(registration);
        return ResponseEntity.ok(ApiResponse.success(history, "Print history fetched successfully"));
    }

    @GetMapping("/count/{registrationId}")
    public ResponseEntity<ApiResponse<Long>> getPrintCount(@PathVariable String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        long count = badgeService.getEventPrintCount(registration);
        return ResponseEntity.ok(ApiResponse.success(count, "Print count fetched successfully"));
    }
}