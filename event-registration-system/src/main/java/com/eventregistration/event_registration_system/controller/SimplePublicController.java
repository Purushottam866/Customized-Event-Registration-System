package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.entity.SimpleForm;
import com.eventregistration.event_registration_system.service.SimpleFormService;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class SimplePublicController {

    private final SimpleFormService formService;
    private final JsonConverter jsonConverter;

    @GetMapping("/forms")
    public ResponseEntity<ApiResponse<List<SimpleForm>>> getPublishedForms() {
        return ResponseEntity.ok(ApiResponse.success(
            formService.getPublishedForms(), 
            "Published forms fetched successfully"
        ));
    }

    @GetMapping("/forms/{id}")
    public ResponseEntity<ApiResponse<SimpleForm>> getPublishedForm(@PathVariable Long id) {
        SimpleForm form = formService.getPublishedForm(id);
        return ResponseEntity.ok(ApiResponse.success(form, "Form fetched successfully"));
    }

    @GetMapping("/forms/{id}/fields")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPublishedFormFields(@PathVariable Long id) {
        SimpleForm form = formService.getPublishedForm(id);
        List<Map<String, Object>> fields = jsonConverter.fromJson(form.getFields(), List.class);
        return ResponseEntity.ok(ApiResponse.success(fields, "Form fields fetched successfully"));
    }
}