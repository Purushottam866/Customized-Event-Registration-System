package com.eventregistration.event_registration_system.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgePrintRequest {
    
    @NotNull(message = "Selected fields are required")
    private List<String> selectedFields;
    
    private List<Long> registrationIds; // For bulk printing
}