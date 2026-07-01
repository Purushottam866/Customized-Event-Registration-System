package com.eventregistration.event_registration_system.dto.response;

import com.eventregistration.event_registration_system.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate eventDate;
    private String location;
    private EventStatus status;
    private Long createdById;
    private String createdByUsername;
    private String createdByFullName;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private Integer maxAttendees;
    private String eventLogoUrl;
    private List<String> badgeFields;  // NEW
}