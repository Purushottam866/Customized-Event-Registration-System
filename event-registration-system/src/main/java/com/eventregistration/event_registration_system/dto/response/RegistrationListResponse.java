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
public class RegistrationListResponse {
    private Long id;
    private String registrationId;
    private Boolean checkInStatus;
    private LocalDateTime checkedInAt;
    private LocalDateTime createdAt;
    private Map<String, Object> formData;
    private String name;
    private String email;
    private String mobile;
    private String company;
}