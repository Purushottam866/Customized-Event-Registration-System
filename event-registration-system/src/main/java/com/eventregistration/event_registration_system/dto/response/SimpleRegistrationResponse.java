package com.eventregistration.event_registration_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRegistrationResponse {
    private Long id;
    private String registrationId;
    private String qrCode;
    private LocalDateTime createdAt;
    private Map<String, Object> formData;
    private String message;  // Only used for registration success response
}