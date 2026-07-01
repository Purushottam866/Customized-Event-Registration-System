package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.SimpleBadgePrint;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.repository.SimpleBadgePrintRepository;
import com.eventregistration.event_registration_system.repository.SimpleRegistrationRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleBadgePrintService {

    private final SimpleBadgePrintRepository badgePrintRepository;
    private final SimpleRegistrationService registrationService;
    private final SimpleRegistrationRepository registrationRepository;
    private final JsonConverter jsonConverter;

    @Transactional
    public void logBadgePrint(String registrationId, List<String> selectedFields, Long printedByUserId) {
        SimpleRegistration registration = registrationService.getRegistrationById(registrationId);
        
        SimpleBadgePrint badgePrint = new SimpleBadgePrint();
        badgePrint.setRegistration(registration);
        badgePrint.setPrintedBy(printedByUserId);
        badgePrint.setSelectedFields(jsonConverter.toJson(selectedFields));
        
        badgePrintRepository.save(badgePrint);
        log.info("Simple badge print logged for registration: {}", registrationId);
    }

    public long getPrintCount(String registrationId) {
        SimpleRegistration registration = registrationService.getRegistrationById(registrationId);
        return badgePrintRepository.countByRegistration(registration);
    }

    public List<SimpleBadgePrint> getPrintHistory(String registrationId) {
        SimpleRegistration registration = registrationService.getRegistrationById(registrationId);
        return badgePrintRepository.findByRegistration(registration);
    }

    public long getTotalPrintCount() {
        return badgePrintRepository.count();
    }

    public long getTotalPrintCountByForm(Long formId) {
        return badgePrintRepository.countByFormId(formId);
    }

    public long getTotalRegistrationsCount() {
        return registrationRepository.count();
    }

    public long getTotalRegistrationsCountByForm(Long formId) {
        return registrationRepository.countByFormId(formId);
    }

    // NEW: Get all badge prints with registration data for export
    public List<Map<String, Object>> getAllBadgePrintsForExport() {
        List<SimpleBadgePrint> badgePrints = badgePrintRepository.findAllByOrderByPrintedAtDesc();
        
        return badgePrints.stream()
                .map(bp -> {
                    Map<String, Object> formData = jsonConverter.fromJsonToMap(bp.getRegistration().getFormData());
                    Map<String, Object> exportData = new java.util.HashMap<>();
                    
                    // Registration details
                    exportData.put("registrationId", bp.getRegistration().getRegistrationId());
                    exportData.put("name", formData.getOrDefault("name", "N/A"));
                    exportData.put("email", formData.getOrDefault("email", "N/A"));
                    exportData.put("mobile", formData.getOrDefault("mobile", "N/A"));
                    exportData.put("company", formData.getOrDefault("company", "N/A"));
                    exportData.put("designation", formData.getOrDefault("designation", "N/A"));
                    exportData.put("city", formData.getOrDefault("city", "N/A"));
                    
                    // Print details
                    exportData.put("printedAt", bp.getPrintedAt());
                    exportData.put("printedBy", bp.getPrintedBy());
                    exportData.put("selectedFields", bp.getSelectedFields());
                    
                    return exportData;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getBadgePrintsForExportByForm(Long formId) {
        List<SimpleBadgePrint> badgePrints = badgePrintRepository.findByFormIdOrderByPrintedAtDesc(formId);
        
        return badgePrints.stream()
                .map(bp -> {
                    Map<String, Object> formData = jsonConverter.fromJsonToMap(bp.getRegistration().getFormData());
                    Map<String, Object> exportData = new java.util.HashMap<>();
                    
                    // Registration details
                    exportData.put("registrationId", bp.getRegistration().getRegistrationId());
                    exportData.put("name", formData.getOrDefault("name", "N/A"));
                    exportData.put("email", formData.getOrDefault("email", "N/A"));
                    exportData.put("mobile", formData.getOrDefault("mobile", "N/A"));
                    exportData.put("company", formData.getOrDefault("company", "N/A"));
                    exportData.put("designation", formData.getOrDefault("designation", "N/A"));
                    exportData.put("city", formData.getOrDefault("city", "N/A"));
                    
                    // Print details
                    exportData.put("printedAt", bp.getPrintedAt());
                    exportData.put("printedBy", bp.getPrintedBy());
                    exportData.put("selectedFields", bp.getSelectedFields());
                    
                    return exportData;
                })
                .collect(Collectors.toList());
    }
}