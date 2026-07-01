package com.eventregistration.event_registration_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnSpotRegistrationRequest {

    @NotNull(message = "Form data is required")
    private Map<String, Object> formData;

    @NotNull(message = "Badge fields are required")
    private java.util.List<String> badgeFields;
}