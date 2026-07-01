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
public class BadgePrintExportDTO {
    private String registrationId;
    private String name;
    private String email;
    private String mobile;
    private String company;
    private String designation;
    private String city;
    private LocalDateTime printedAt;
    private String printedBy;
    private String selectedFields;
}