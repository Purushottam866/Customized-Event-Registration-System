package com.eventregistration.event_registration_system.dto.request;

import com.eventregistration.event_registration_system.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    
    @NotBlank(message = "Event name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;
    
    private String location;
    
    private EventStatus status;
    
    private Integer maxAttendees;
    
    private String eventLogoUrl;
    
    // NEW: Badge fields to print (optional, defaults to ["Name", "Company"])
    private List<String> badgeFields;
}