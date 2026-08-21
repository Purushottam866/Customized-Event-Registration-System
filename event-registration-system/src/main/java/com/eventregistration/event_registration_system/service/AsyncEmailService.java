package com.eventregistration.event_registration_system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailService {

    private final EmailService emailService;

    @Async
    public void sendEmailAsync(String registrationId) {
        try {
            log.info("Async email sending started for: {}", registrationId);
            boolean success = emailService.sendRegistrationEmail(registrationId);
            if (success) {
                log.info("Async email sent successfully for: {}", registrationId);
            } else {
                log.warn("Async email failed for: {}", registrationId);
            }
        } catch (Exception e) {
            log.error("Async email error for {}: {}", registrationId, e.getMessage());
        }
    }
}