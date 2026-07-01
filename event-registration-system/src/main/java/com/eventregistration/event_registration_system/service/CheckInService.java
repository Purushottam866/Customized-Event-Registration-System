package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final RegistrationService registrationService;
    private final JsonConverter jsonConverter;

    /**
     * Search for registrations and format for display
     */
    public List<Map<String, Object>> searchForCheckIn(Long eventId, String searchTerm) {
        List<Registration> registrations;
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            registrations = registrationService.getRegistrationsByEvent(eventId);
        } else {
            registrations = registrationService.searchRegistrations(eventId, searchTerm.trim());
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Registration registration : registrations) {
            Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
            Map<String, Object> displayData = new HashMap<>();
            
            // Basic info
            displayData.put("id", registration.getId());
            displayData.put("registrationId", registration.getRegistrationId());
            displayData.put("checkInStatus", registration.getCheckInStatus());
            displayData.put("checkedInAt", registration.getCheckedInAt());
            displayData.put("createdAt", registration.getCreatedAt());
            
            // Extract common fields from form data
            displayData.put("name", formData.getOrDefault("name", "N/A"));
            displayData.put("email", formData.getOrDefault("email", "N/A"));
            displayData.put("mobile", formData.getOrDefault("mobile", "N/A"));
            displayData.put("company", formData.getOrDefault("company", "N/A"));
            
            // Store all form data for badge generation
            displayData.put("formData", formData);
            
            results.add(displayData);
        }
        
        return results;
    }

    /**
     * Get registration details for check-in desk
     */
    public Map<String, Object> getRegistrationDetails(String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
        
        Map<String, Object> details = new HashMap<>();
        details.put("registrationId", registration.getRegistrationId());
        details.put("checkInStatus", registration.getCheckInStatus());
        details.put("checkedInAt", registration.getCheckedInAt());
        details.put("qrCode", registration.getQrCode());
        details.put("formData", formData);
        
        return details;
    }

    /**
     * Get all data for QR code (for scanning)
     */
    public Map<String, Object> getQRData(String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        return jsonConverter.fromJsonToMap(registration.getFormData());
    }
}