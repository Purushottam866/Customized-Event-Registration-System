package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.request.SimpleFormRequest;
import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.entity.SimpleForm;
import com.eventregistration.event_registration_system.entity.User;
import com.eventregistration.event_registration_system.service.AuthService;
import com.eventregistration.event_registration_system.service.SimpleFormService;
import com.eventregistration.event_registration_system.util.JsonConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple/forms")
@RequiredArgsConstructor
@Slf4j
public class SimpleFormController {

    private final SimpleFormService formService;
    private final AuthService authService;
    private final JsonConverter jsonConverter;

    @PostMapping
    public ResponseEntity<ApiResponse<SimpleForm>> createForm(@Valid @RequestBody SimpleFormRequest request,
                                                               Authentication authentication) {
        String email = authentication.getName();
        User admin = authService.getUserByEmail(email);
        
        SimpleForm form = new SimpleForm();
        form.setTitle(request.getTitle());
        form.setDescription(request.getDescription());
        form.setFields(jsonConverter.toJson(request.getFields()));
        
        if (request.getBadgeFields() != null && !request.getBadgeFields().isEmpty()) {
            form.setBadgeFields(jsonConverter.toJson(request.getBadgeFields()));
        }
        
        SimpleForm created = formService.createForm(form, admin.getId());
        log.info("Simple form created by admin: {}", admin.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Form created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SimpleForm>>> getAllForms() {
        return ResponseEntity.ok(ApiResponse.success(formService.getAllForms(), "Forms fetched successfully"));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<SimpleForm>>> getPublishedForms() {
        return ResponseEntity.ok(ApiResponse.success(formService.getPublishedForms(), "Published forms fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SimpleForm>> getForm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(formService.getFormById(id), "Form fetched successfully"));
    }

    @GetMapping("/{id}/fields")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFormFields(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(formService.getFormFields(id), "Form fields fetched successfully"));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<SimpleForm>> publishForm(@PathVariable Long id) {
        SimpleForm form = formService.publishForm(id);
        return ResponseEntity.ok(ApiResponse.success(form, "Form published successfully"));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<SimpleForm>> unpublishForm(@PathVariable Long id) {
        SimpleForm form = formService.unpublishForm(id);
        return ResponseEntity.ok(ApiResponse.success(form, "Form unpublished successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SimpleForm>> updateForm(@PathVariable Long id,
                                                               @Valid @RequestBody SimpleFormRequest request) {
        SimpleForm form = new SimpleForm();
        form.setTitle(request.getTitle());
        form.setDescription(request.getDescription());
        form.setFields(jsonConverter.toJson(request.getFields()));
        
        if (request.getBadgeFields() != null && !request.getBadgeFields().isEmpty()) {
            form.setBadgeFields(jsonConverter.toJson(request.getBadgeFields()));
        }
        
        SimpleForm updated = formService.updateForm(id, form);
        return ResponseEntity.ok(ApiResponse.success(updated, "Form updated successfully"));
    }

    @PutMapping("/{id}/badge-fields")
    public ResponseEntity<ApiResponse<SimpleForm>> updateBadgeFields(@PathVariable Long id,
                                                                      @RequestBody List<String> badgeFields) {
        SimpleForm form = formService.updateBadgeFields(id, badgeFields);
        return ResponseEntity.ok(ApiResponse.success(form, "Badge fields updated successfully"));
    }

    @GetMapping("/{id}/badge-fields")
    public ResponseEntity<ApiResponse<List<String>>> getBadgeFields(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(formService.getBadgeFields(id), "Badge fields fetched successfully"));
    }

    // ========== DELETE FORM ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        log.info("Form deleted: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null, "Form deleted successfully"));
    }
}