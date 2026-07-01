package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.entity.FormTemplate;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import com.eventregistration.event_registration_system.exception.ResourceNotFoundException;
import com.eventregistration.event_registration_system.repository.FormTemplateRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormService {

    private final FormTemplateRepository formTemplateRepository;
    private final EventService eventService;
    private final JsonConverter jsonConverter;

    /**
     * Create or update form template for an event
     */
    @Transactional
    public FormTemplate createOrUpdateFormTemplate(Long eventId, String templateName, List<Map<String, Object>> fields) {
        Event event = eventService.getEventById(eventId);
        
        // Validate fields
        if (fields == null || fields.isEmpty()) {
            throw new BadRequestException("Form fields cannot be empty");
        }

        // Check if template already exists
        FormTemplate existingTemplate = formTemplateRepository.findByEventAndIsActiveTrue(event).orElse(null);
        
        if (existingTemplate != null) {
            // Update existing template
            existingTemplate.setTemplateName(templateName);
            existingTemplate.setFields(jsonConverter.toJson(fields));
            log.info("Updating form template for event: {}", event.getName());
            return formTemplateRepository.save(existingTemplate);
        } else {
            // Create new template
            FormTemplate formTemplate = new FormTemplate();
            formTemplate.setEvent(event);
            formTemplate.setTemplateName(templateName);
            formTemplate.setFields(jsonConverter.toJson(fields));
            formTemplate.setIsActive(true);
            log.info("Creating form template for event: {}", event.getName());
            return formTemplateRepository.save(formTemplate);
        }
    }

    /**
     * Get form template by event ID
     */
    public FormTemplate getFormTemplateByEventId(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return formTemplateRepository.findByEventAndIsActiveTrue(event)
                .orElseThrow(() -> new ResourceNotFoundException("FormTemplate", "eventId", eventId));
    }

    /**
     * Get all form templates for an event
     */
    public List<FormTemplate> getFormTemplatesByEventId(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return formTemplateRepository.findByEvent(event);
    }

    /**
     * Get form fields as list
     */
    public List<Map<String, Object>> getFormFields(Long eventId) {
        FormTemplate template = getFormTemplateByEventId(eventId);
        return jsonConverter.fromJson(template.getFields(), List.class);
    }

    /**
     * Validate form data against template
     */
    public void validateFormData(Long eventId, Map<String, Object> formData) {
        List<Map<String, Object>> fields = getFormFields(eventId);
        
        for (Map<String, Object> field : fields) {
            String fieldName = (String) field.get("label");
            Boolean required = (Boolean) field.getOrDefault("required", false);
            String fieldType = (String) field.get("type");
            
            if (required) {
                Object value = formData.get(fieldName.toLowerCase());
                if (value == null || value.toString().trim().isEmpty()) {
                    throw new BadRequestException("Field '" + fieldName + "' is required");
                }
            }
            
            // Additional validation based on type
            Object value = formData.get(fieldName.toLowerCase());
            if (value != null && !value.toString().trim().isEmpty()) {
                validateFieldType(fieldName, value.toString(), fieldType);
            }
        }
    }

    /**
     * Validate field based on type
     */
    private void validateFieldType(String fieldName, String value, String fieldType) {
        switch (fieldType.toUpperCase()) {
            case "EMAIL":
                if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new BadRequestException("Invalid email format for field: " + fieldName);
                }
                break;
            case "PHONE":
                if (!value.matches("^[0-9]{10,15}$")) {
                    throw new BadRequestException("Invalid phone number format for field: " + fieldName);
                }
                break;
            case "NUMBER":
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("Field '" + fieldName + "' must be a number");
                }
                break;
            case "DATE":
                try {
                    java.time.LocalDate.parse(value);
                } catch (Exception e) {
                    throw new BadRequestException("Field '" + fieldName + "' must be a valid date (YYYY-MM-DD)");
                }
                break;
            default:
                break;
        }
    }

    /**
     * Deactivate form template
     */
    @Transactional
    public void deactivateFormTemplate(Long eventId) {
        FormTemplate template = getFormTemplateByEventId(eventId);
        template.setIsActive(false);
        formTemplateRepository.save(template);
        log.info("Form template deactivated for event ID: {}", eventId);
    }
}