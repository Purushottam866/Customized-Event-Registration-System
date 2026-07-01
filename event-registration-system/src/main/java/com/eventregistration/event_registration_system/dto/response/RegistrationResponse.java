package com.eventregistration.event_registration_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private Long id;
    private String registrationId;
    private String qrCode;
    private Boolean checkInStatus;
    private LocalDateTime checkedInAt;
    private LocalDateTime createdAt;
    private Map<String, Object> formData;
    private String message;
}