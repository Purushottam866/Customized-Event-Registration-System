package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.BadgePrint;
import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.repository.BadgePrintRepository;
import com.eventregistration.event_registration_system.util.BadgeHTMLGenerator;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgePrintRepository badgePrintRepository;
    private final BadgeHTMLGenerator badgeHTMLGenerator;
    private final RegistrationService registrationService;
    private final JsonConverter jsonConverter;

    // ==================== BADGE HTML GENERATION ====================

    public String generateBadgeHTML(String registrationId, List<String> selectedFields, Long printedByUserId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        String eventName = registration.getEvent().getName();
        
        // Log the badge print
        logEventBadgePrint(registration, selectedFields, printedByUserId);
        
        return badgeHTMLGenerator.generateBadgeHTML(registration, selectedFields, eventName);
    }

    public String generateBadgeHTML(Long registrationId, List<String> selectedFields, Long printedByUserId) {
        Registration registration = registrationService.getRegistrationByLongId(registrationId);
        String eventName = registration.getEvent().getName();
        
        // Log the badge print
        logEventBadgePrint(registration, selectedFields, printedByUserId);
        
        return badgeHTMLGenerator.generateBadgeHTML(registration, selectedFields, eventName);
    }

    public String generateBulkBadgeHTML(List<Long> registrationIds, List<String> selectedFields, Long printedByUserId) {
        if (registrationIds == null || registrationIds.isEmpty()) {
            throw new IllegalArgumentException("No registrations selected");
        }
        
        List<Registration> registrations = registrationIds.stream()
                .map(registrationService::getRegistrationByLongId)
                .toList();
        
        String eventName = registrations.get(0).getEvent().getName();
        
        // Log badge prints for all registrations
        for (Registration registration : registrations) {
            logEventBadgePrint(registration, selectedFields, printedByUserId);
        }
        
        return badgeHTMLGenerator.generateBulkBadgeHTML(registrations, selectedFields, eventName);
    }

    // ==================== EVENT MODULE ====================

    @Transactional
    public void logEventBadgePrint(Registration registration, List<String> selectedFields, Long printedByUserId) {
        BadgePrint badgePrint = new BadgePrint();
        badgePrint.setRegistration(registration);
        badgePrint.setPrintedBy(printedByUserId);
        badgePrint.setSelectedFields(jsonConverter.toJson(selectedFields));
        badgePrint.setModuleType("EVENT");
        
        badgePrintRepository.save(badgePrint);
        log.info("Event badge print logged for registration: {}", registration.getRegistrationId());
    }

    public long getEventPrintCount(Registration registration) {
        return badgePrintRepository.countByRegistration(registration);
    }

    public List<BadgePrint> getEventPrintHistory(Registration registration) {
        return badgePrintRepository.findByRegistration(registration);
    }

    // ==================== SIMPLE MODULE ====================

    @Transactional
    public void logSimpleBadgePrint(SimpleRegistration simpleRegistration, List<String> selectedFields, Long printedByUserId) {
        BadgePrint badgePrint = new BadgePrint();
        badgePrint.setSimpleRegistration(simpleRegistration);
        badgePrint.setPrintedBy(printedByUserId);
        badgePrint.setSelectedFields(jsonConverter.toJson(selectedFields));
        badgePrint.setModuleType("SIMPLE");
        
        badgePrintRepository.save(badgePrint);
        log.info("Simple badge print logged for registration: {}", simpleRegistration.getRegistrationId());
    }

    public long getSimplePrintCount(SimpleRegistration simpleRegistration) {
        return badgePrintRepository.countBySimpleRegistration(simpleRegistration);
    }

    public List<BadgePrint> getSimplePrintHistory(SimpleRegistration simpleRegistration) {
        return badgePrintRepository.findBySimpleRegistration(simpleRegistration);
    }

    // ==================== TOTAL COUNTS ====================

    public long getTotalPrintCount(String moduleType) {
        return badgePrintRepository.countByModuleType(moduleType);
    }

    public long getTotalSimplePrintCountByForm(Long formId) {
        return badgePrintRepository.countByFormId(formId);
    }

    // ==================== LEGACY METHODS (For backward compatibility) ====================

    public List<BadgePrint> getPrintHistory(String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        return getEventPrintHistory(registration);
    }

    public int getPrintCount(String registrationId) {
        Registration registration = registrationService.getRegistrationById(registrationId);
        return (int) getEventPrintCount(registration);
    }
}