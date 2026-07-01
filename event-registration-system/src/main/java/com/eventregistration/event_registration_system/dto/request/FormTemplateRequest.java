package com.eventregistration.event_registration_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    private String templateName;
    
    @NotNull(message = "Fields are required")
    private List<Map<String, Object>> fields;
}