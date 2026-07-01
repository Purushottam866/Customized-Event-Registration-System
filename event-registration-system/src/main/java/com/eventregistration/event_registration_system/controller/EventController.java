package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.EventRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.dto.response.EventResponse;
import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.entity.User;
import com.eventregistration.event_registration_system.enums.EventStatus;
import com.eventregistration.event_registration_system.service.AuthService;
import com.eventregistration.event_registration_system.service.EventService;
import com.eventregistration.event_registration_system.util.JsonConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final AuthService authService;
    private final JsonConverter jsonConverter;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request, 
                                                                   Authentication authentication) {
        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());
        event.setStatus(request.getStatus() != null ? request.getStatus() : EventStatus.DRAFT);
        event.setMaxAttendees(request.getMaxAttendees());
        event.setEventLogoUrl(request.getEventLogoUrl());
        
        // Set badge fields from request or default
        if (request.getBadgeFields() != null && !request.getBadgeFields().isEmpty()) {
            event.setBadgeFields(jsonConverter.toJson(request.getBadgeFields()));
        }
        
        String email = authentication.getName();
        User user = authService.getUserByEmail(email);
        
        Event created = eventService.createEvent(event, user.getId());
        log.info("Event created: {} by user: {}", created.getName(), email);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(convertToResponse(created), "Event created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventResponse> responses = events.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Events fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(event), "Event fetched successfully"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByStatus(@PathVariable EventStatus status) {
        List<Event> events = eventService.getEventsByStatus(status);
        List<EventResponse> responses = events.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Events fetched successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getActiveEvents() {
        List<Event> events = eventService.getActiveEvents();
        List<EventResponse> responses = events.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Active events fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(@PathVariable Long id, 
                                                                  @Valid @RequestBody EventRequest request) {
        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());
        event.setMaxAttendees(request.getMaxAttendees());
        event.setEventLogoUrl(request.getEventLogoUrl());
        
        if (request.getBadgeFields() != null && !request.getBadgeFields().isEmpty()) {
            event.setBadgeFields(jsonConverter.toJson(request.getBadgeFields()));
        }
        
        Event updated = eventService.updateEvent(id, event);
        log.info("Event updated: {}", updated.getName());
        
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(updated), "Event updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EventResponse>> updateEventStatus(@PathVariable Long id, 
                                                                        @RequestParam EventStatus status) {
        Event event = eventService.updateEventStatus(id, status);
        log.info("Event status updated: {} -> {}", event.getName(), status);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(event), "Event status updated successfully"));
    }

    /**
     * Update badge fields for an event
     */
    @PutMapping("/{id}/badge-fields")
    public ResponseEntity<ApiResponse<EventResponse>> updateBadgeFields(
            @PathVariable Long id,
            @RequestBody List<String> badgeFields) {
        Event event = eventService.updateBadgeFields(id, badgeFields);
        log.info("Badge fields updated for event: {}", event.getName());
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(event), "Badge fields updated successfully"));
    }

    /**
     * Get badge fields for an event
     */
    @GetMapping("/{id}/badge-fields")
    public ResponseEntity<ApiResponse<List<String>>> getBadgeFields(@PathVariable Long id) {
        List<String> badgeFields = eventService.getBadgeFields(id);
        return ResponseEntity.ok(ApiResponse.success(badgeFields, "Badge fields fetched successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        log.info("Event deleted: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }

    /**
     * Convert Event entity to EventResponse DTO
     */
    private EventResponse convertToResponse(Event event) {
        User createdBy = event.getCreatedBy();
        List<String> badgeFields = null;
        if (event.getBadgeFields() != null) {
            badgeFields = jsonConverter.fromJson(event.getBadgeFields(), List.class);
        }
        
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .status(event.getStatus())
                .createdById(createdBy != null ? createdBy.getId() : null)
                .createdByUsername(createdBy != null ? createdBy.getUsername() : null)
                .createdByFullName(createdBy != null ? createdBy.getFullName() : null)
                .createdByEmail(createdBy != null ? createdBy.getEmail() : null)
                .createdAt(event.getCreatedAt())
                .maxAttendees(event.getMaxAttendees())
                .eventLogoUrl(event.getEventLogoUrl())
                .badgeFields(badgeFields)
                .build();
    }
}