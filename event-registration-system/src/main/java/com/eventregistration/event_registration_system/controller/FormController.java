package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.FormTemplateRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.entity.FormTemplate;
import com.eventregistration.event_registration_system.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/form")
@RequiredArgsConstructor
@Slf4j
public class FormController {

    private final FormService formService;

    @PostMapping
    public ResponseEntity<ApiResponse<FormTemplate>> createOrUpdateForm(@PathVariable Long eventId,
                                                                        @Valid @RequestBody FormTemplateRequest request) {
        FormTemplate template = formService.createOrUpdateFormTemplate(
            eventId,
            request.getTemplateName(),
            request.getFields()
        );
        log.info("Form template created/updated for event: {}", eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(template, "Form template saved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FormTemplate>> getFormTemplate(@PathVariable Long eventId) {
        FormTemplate template = formService.getFormTemplateByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success(template, "Form template fetched successfully"));
    }

    @GetMapping("/fields")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFormFields(@PathVariable Long eventId) {
        List<Map<String, Object>> fields = formService.getFormFields(eventId);
        return ResponseEntity.ok(ApiResponse.success(fields, "Form fields fetched successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FormTemplate>>> getAllFormTemplates(@PathVariable Long eventId) {
        List<FormTemplate> templates = formService.getFormTemplatesByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success(templates, "Form templates fetched successfully"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deactivateFormTemplate(@PathVariable Long eventId) {
        formService.deactivateFormTemplate(eventId);
        log.info("Form template deactivated for event: {}", eventId);
        return ResponseEntity.ok(ApiResponse.success(null, "Form template deactivated successfully"));
    }
}