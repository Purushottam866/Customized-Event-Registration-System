package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.dto.response.RegistrationListResponse;
import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.entity.FormTemplate;
import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import com.eventregistration.event_registration_system.exception.DuplicateRegistrationException;
import com.eventregistration.event_registration_system.exception.ResourceNotFoundException;
import com.eventregistration.event_registration_system.repository.RegistrationRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import com.eventregistration.event_registration_system.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final FormService formService;
    private final QRCodeGenerator qrCodeGenerator;
    private final JsonConverter jsonConverter;

    @Transactional
    public Registration registerAttendee(Long eventId, Map<String, Object> formData) {
        if (!eventService.isEventActive(eventId)) {
            throw new BadRequestException("Event is not active for registration");
        }

        Event event = eventService.getEventById(eventId);
        FormTemplate formTemplate = formService.getFormTemplateByEventId(eventId);
        formService.validateFormData(eventId, formData);
        
        String email = (String) formData.get("email");
        String mobile = (String) formData.get("mobile");
        
        if (email != null || mobile != null) {
            List<Registration> existingRegistrations = registrationRepository.findByEvent(event);
            for (Registration existing : existingRegistrations) {
                Map<String, Object> existingData = jsonConverter.fromJsonToMap(existing.getFormData());
                if (email != null && email.equals(existingData.get("email"))) {
                    throw new DuplicateRegistrationException("Email already registered for this event: " + email);
                }
                if (mobile != null && mobile.equals(existingData.get("mobile"))) {
                    throw new DuplicateRegistrationException("Mobile number already registered for this event: " + mobile);
                }
            }
        }
        
        long count = registrationRepository.count() + 1;
        String registrationId = "REG-" + String.format("%06d", count);
        String formDataJson = jsonConverter.toJson(formData);
        
        List<Map<String, Object>> formFields = formService.getFormFields(eventId);
        String qrData = qrCodeGenerator.generateQRDataWithValuesOnly(formData, formFields);
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(qrData);
        
        Registration registration = new Registration();
        registration.setRegistrationId(registrationId);
        registration.setEvent(event);
        registration.setFormTemplate(formTemplate);
        registration.setFormData(formDataJson);
        registration.setQrCode(qrCodeBase64);
        registration.setCheckInStatus(false);
        registration.setIsActive(true);
        
        log.info("New registration created: {} for event: {}", registrationId, event.getName());
        return registrationRepository.save(registration);
    }

    public Registration getRegistrationById(String registrationId) {
        return registrationRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "registrationId", registrationId));
    }

    public Registration getRegistrationByLongId(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
    }

    public List<RegistrationListResponse> getRegistrationsByEventAsDTO(Long eventId) {
        Event event = eventService.getEventById(eventId);
        List<Registration> registrations = registrationRepository.findByEvent(event);
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Registration> getRegistrationsByEvent(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return registrationRepository.findByEvent(event);
    }

    public List<RegistrationListResponse> searchRegistrationsAsDTO(Long eventId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getRegistrationsByEventAsDTO(eventId);
        }
        List<Registration> registrations = registrationRepository.searchRegistrations(eventId, searchTerm.trim());
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Registration> searchRegistrations(Long eventId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getRegistrationsByEvent(eventId);
        }
        return registrationRepository.searchRegistrations(eventId, searchTerm.trim());
    }

    @Transactional
    public Registration checkInAttendee(String registrationId) {
        Registration registration = getRegistrationById(registrationId);
        
        if (registration.getCheckInStatus()) {
            throw new BadRequestException("Attendee already checked in");
        }
        
        registration.setCheckInStatus(true);
        registration.setCheckedInAt(LocalDateTime.now());
        
        log.info("Attendee checked in: {}", registrationId);
        return registrationRepository.save(registration);
    }

    @Transactional
    public Registration checkInAttendeeByLongId(Long id) {
        Registration registration = getRegistrationByLongId(id);
        return checkInAttendee(registration.getRegistrationId());
    }

    public List<RegistrationListResponse> getCheckedInAttendeesAsDTO(Long eventId) {
        Event event = eventService.getEventById(eventId);
        List<Registration> registrations = registrationRepository.findByEventAndCheckInStatus(event, true);
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Registration> getCheckedInAttendees(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return registrationRepository.findByEventAndCheckInStatus(event, true);
    }

    public List<RegistrationListResponse> getPendingCheckInAttendeesAsDTO(Long eventId) {
        Event event = eventService.getEventById(eventId);
        List<Registration> registrations = registrationRepository.findByEventAndCheckInStatus(event, false);
        return registrations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Registration> getPendingCheckInAttendees(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return registrationRepository.findByEventAndCheckInStatus(event, false);
    }

    public long getRegistrationCount(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return registrationRepository.findByEvent(event).size();
    }

    @Transactional
    public void bulkCheckIn(List<String> registrationIds) {
        for (String registrationId : registrationIds) {
            try {
                checkInAttendee(registrationId);
            } catch (Exception e) {
                log.warn("Failed to check-in {}: {}", registrationId, e.getMessage());
            }
        }
    }

    // NEW: Regenerate QR Code for a registration
    @Transactional
    public Registration regenerateQRCode(String registrationId) {
        Registration registration = getRegistrationById(registrationId);
        
        // Get form fields for QR generation
        Long eventId = registration.getEvent().getId();
        List<Map<String, Object>> formFields = formService.getFormFields(eventId);
        
        // Parse form data
        Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
        
        // Generate new QR code with values only
        String qrData = qrCodeGenerator.generateQRDataWithValuesOnly(formData, formFields);
        String qrCodeBase64 = qrCodeGenerator.generateQRCodeBase64(qrData);
        
        // Update registration
        registration.setQrCode(qrCodeBase64);
        
        log.info("QR Code regenerated for registration: {} with data: {}", registrationId, qrData);
        return registrationRepository.save(registration);
    }

    private RegistrationListResponse convertToDTO(Registration registration) {
        Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
        
        return RegistrationListResponse.builder()
                .id(registration.getId())
                .registrationId(registration.getRegistrationId())
                .checkInStatus(registration.getCheckInStatus())
                .checkedInAt(registration.getCheckedInAt())
                .createdAt(registration.getCreatedAt())
                .formData(formData)
                .name((String) formData.getOrDefault("name", "N/A"))
                .email((String) formData.getOrDefault("email", "N/A"))
                .mobile((String) formData.getOrDefault("mobile", "N/A"))
                .company((String) formData.getOrDefault("company", "N/A"))
                .build();
    }
}