package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.SimpleForm;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import com.eventregistration.event_registration_system.exception.ResourceNotFoundException;
import com.eventregistration.event_registration_system.repository.SimpleFormRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleFormService {

    private final SimpleFormRepository simpleFormRepository;
    private final JsonConverter jsonConverter;

    @Transactional
    public SimpleForm createForm(SimpleForm form, Long adminUserId) {
        form.setCreatedBy(adminUserId);
        form.setIsPublished(false);
        
        if (form.getBadgeFields() == null) {
            form.setBadgeFields("[\"Name\", \"Company\"]");
        }
        
        log.info("Simple form created: {}", form.getTitle());
        return simpleFormRepository.save(form);
    }

    public SimpleForm getFormById(Long id) {
        return simpleFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SimpleForm", "id", id));
    }

    public SimpleForm getPublishedForm(Long id) {
        return simpleFormRepository.findByIdAndIsPublishedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Published SimpleForm", "id", id));
    }

    public List<SimpleForm> getAllForms() {
        return simpleFormRepository.findAll();
    }

    public List<SimpleForm> getPublishedForms() {
        return simpleFormRepository.findByIsPublishedTrue();
    }

    @Transactional
    public SimpleForm publishForm(Long id) {
        SimpleForm form = getFormById(id);
        
        if (form.getIsPublished()) {
            throw new BadRequestException("Form is already published");
        }
        
        form.setIsPublished(true);
        form.setPublishedAt(LocalDateTime.now());
        
        log.info("Form published: {}", form.getTitle());
        return simpleFormRepository.save(form);
    }

    @Transactional
    public SimpleForm unpublishForm(Long id) {
        SimpleForm form = getFormById(id);
        form.setIsPublished(false);
        log.info("Form unpublished: {}", form.getTitle());
        return simpleFormRepository.save(form);
    }

    @Transactional
    public SimpleForm updateForm(Long id, SimpleForm updatedForm) {
        SimpleForm existingForm = getFormById(id);
        
        if (existingForm.getIsPublished()) {
            throw new BadRequestException("Cannot update a published form. Unpublish it first.");
        }
        
        existingForm.setTitle(updatedForm.getTitle());
        existingForm.setDescription(updatedForm.getDescription());
        existingForm.setFields(updatedForm.getFields());
        
        if (updatedForm.getBadgeFields() != null) {
            existingForm.setBadgeFields(updatedForm.getBadgeFields());
        }
        
        log.info("Form updated: {}", existingForm.getTitle());
        return simpleFormRepository.save(existingForm);
    }

    // ========== DELETE FORM (WORKS FOR BOTH PUBLISHED AND UNPUBLISHED) ==========
    @Transactional
    public void deleteForm(Long id) {
        SimpleForm form = getFormById(id);
        simpleFormRepository.delete(form);
        log.info("Form deleted: {} (Published: {})", form.getTitle(), form.getIsPublished());
    }

    public List<Map<String, Object>> getFormFields(Long formId) {
        SimpleForm form = getFormById(formId);
        return jsonConverter.fromJson(form.getFields(), List.class);
    }

    public List<String> getBadgeFields(Long formId) {
        SimpleForm form = getFormById(formId);
        if (form.getBadgeFields() == null) {
            return List.of("Name", "Company");
        }
        return jsonConverter.fromJson(form.getBadgeFields(), List.class);
    }

    @Transactional
    public SimpleForm updateBadgeFields(Long formId, List<String> badgeFields) {
        SimpleForm form = getFormById(formId);
        form.setBadgeFields(jsonConverter.toJson(badgeFields));
        log.info("Badge fields updated for form: {}", form.getTitle());
        return simpleFormRepository.save(form);
    }
}