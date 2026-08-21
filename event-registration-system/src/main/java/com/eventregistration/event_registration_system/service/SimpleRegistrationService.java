package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.SimpleForm;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import com.eventregistration.event_registration_system.exception.DuplicateRegistrationException;
import com.eventregistration.event_registration_system.exception.ResourceNotFoundException;
import com.eventregistration.event_registration_system.repository.SimpleRegistrationRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import com.eventregistration.event_registration_system.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleRegistrationService {

    private final SimpleRegistrationRepository registrationRepository;
    private final SimpleFormService formService;
    private final QRCodeGenerator qrCodeGenerator;
    private final JsonConverter jsonConverter;
    private final AsyncEmailService asyncEmailService;  // Changed from EmailService to AsyncEmailService

    @Transactional
    public SimpleRegistration registerUser(Long formId, Map<String, Object> formData) {
        // Get published form
        SimpleForm form = formService.getPublishedForm(formId);
        
        // Validate form data
        validateFormData(formId, formData);
        
        // Check for duplicate registration (based on email or mobile)
        String email = (String) formData.get("email address");
        String mobile = (String) formData.get("phone number");
        
        if (email != null || mobile != null) {
            List<SimpleRegistration> existingRegistrations = registrationRepository.findByForm(form);
            for (SimpleRegistration existing : existingRegistrations) {
                Map<String, Object> existingData = jsonConverter.fromJsonToMap(existing.getFormData());
                if (email != null && email.equals(existingData.get("email address"))) {
                    throw new DuplicateRegistrationException("Email already registered: " + email);
                }
                if (mobile != null && mobile.equals(existingData.get("phone number"))) {
                    throw new DuplicateRegistrationException("Mobile number already registered: " + mobile);
                }
            }
        }
        
        // Generate registration ID
        long count = registrationRepository.count() + 1;
        String registrationId = "SR-" + String.format("%06d", count);
        
        // Convert form data to JSON
        String formDataJson = jsonConverter.toJson(formData);
        
        // Get form fields for QR generation
        List<Map<String, Object>> formFields = formService.getFormFields(formId);
        
        // Generate QR code with values only
        String qrData = qrCodeGenerator.generateQRDataWithValuesOnly(formData, formFields);
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(qrData);
        
        // Create registration
        SimpleRegistration registration = new SimpleRegistration();
        registration.setRegistrationId(registrationId);
        registration.setForm(form);
        registration.setFormData(formDataJson);
        registration.setQrCode(qrCodeBase64);
        registration.setEmailSent(false);
        registration.setEmailRetryCount(0);
        
        log.info("New simple registration created: {}", registrationId);
        SimpleRegistration savedRegistration = registrationRepository.save(registration);
        
        // ===== SEND EMAIL ASYNCHRONOUSLY =====
        try {
            asyncEmailService.sendEmailAsync(registrationId);
            log.info("Email queued for registration: {}", registrationId);
        } catch (Exception e) {
            log.error("Error queuing email for {}: {}", registrationId, e.getMessage());
        }
        
        return savedRegistration;
    }

    public SimpleRegistration getRegistrationById(String registrationId) {
        return registrationRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("SimpleRegistration", "registrationId", registrationId));
    }

    public SimpleRegistration getRegistrationByLongId(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SimpleRegistration", "id", id));
    }

    public List<SimpleRegistration> getRegistrationsByForm(Long formId) {
        SimpleForm form = formService.getFormById(formId);
        return registrationRepository.findByFormOrderByCreatedAtDesc(form);
    }

    public List<SimpleRegistration> searchRegistrations(Long formId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getRegistrationsByForm(formId);
        }
        return registrationRepository.searchRegistrations(formId, searchTerm.trim());
    }

    public long getRegistrationCount(Long formId) {
        SimpleForm form = formService.getFormById(formId);
        return registrationRepository.findByForm(form).size();
    }

    // ==================== TOTAL COUNTS ====================

    public long getTotalRegistrationsCount() {
        return registrationRepository.count();
    }

    public long getTotalRegistrationsCountByForm(Long formId) {
        return registrationRepository.countByFormId(formId);
    }

    private void validateFormData(Long formId, Map<String, Object> formData) {
        List<Map<String, Object>> fields = formService.getFormFields(formId);
        
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

    private void validateFieldType(String fieldName, String value, String fieldType) {
        switch (fieldType.toUpperCase()) {
            case "EMAIL":
                // Allow alphanumeric + special chars in local part and domain
                // Domain can contain letters, numbers, dots, and hyphens
                if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    throw new BadRequestException("Invalid email format for field: " + fieldName);
                }
                break;
            case "PHONE":
                String cleanedPhone = value.replaceAll("[^0-9]", "").trim();
                if (!cleanedPhone.matches("^[0-9]{10,15}$")) {
                    throw new BadRequestException("Invalid phone number format for field: " + fieldName + ". Expected 10-15 digits.");
                }
                break;
            case "NUMBER":
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("Field '" + fieldName + "' must be a number");
                }
                break;
            default:
                break;
        }
    }
}