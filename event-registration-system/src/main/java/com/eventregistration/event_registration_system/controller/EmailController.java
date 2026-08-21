package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.service.AsyncEmailService;
import com.eventregistration.event_registration_system.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simple/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final AsyncEmailService asyncEmailService;
    private final EmailService emailService;

    /**
     * Send or Resend registration confirmation email to a delegate (Async)
     * 
     * @param registrationId - Registration ID (e.g., SR-000001)
     * @param authentication - Admin authentication
     * @return Success or failure response
     */
    @PostMapping("/send/{registrationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendEmail(
            @PathVariable String registrationId,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("Admin {} requested email send for registration: {}", adminEmail, registrationId);

        // Get current status
        Map<String, Object> status = emailService.getEmailStatus(registrationId);
        boolean alreadySent = (boolean) status.get("emailSent");

        if (alreadySent) {
            // Resend - synchronous (for retry)
            boolean success = emailService.resendRegistrationEmail(registrationId);
            String message = success ? "Email resent successfully" : "Failed to resend email";
            Map<String, Object> updatedStatus = emailService.getEmailStatus(registrationId);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success(updatedStatus, message));
            } else {
                return ResponseEntity.status(500).body(ApiResponse.error(500, message, "/api/simple/email/send/" + registrationId));
            }
        } else {
            // Send asynchronously (first time)
            try {
                asyncEmailService.sendEmailAsync(registrationId);
                Map<String, Object> response = new HashMap<>();
                response.put("registrationId", registrationId);
                response.put("message", "Email queued for sending");
                response.put("status", "QUEUED");
                return ResponseEntity.ok(ApiResponse.success(response, "Email queued successfully"));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(ApiResponse.error(500, "Failed to queue email: " + e.getMessage(), "/api/simple/email/send/" + registrationId));
            }
        }
    }

    /**
     * Get email status for a delegate
     * 
     * @param registrationId - Registration ID (e.g., SR-000001)
     * @return Email status details
     */
    @GetMapping("/status/{registrationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailStatus(
            @PathVariable String registrationId,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("Admin {} requested email status for registration: {}", adminEmail, registrationId);

        Map<String, Object> status = emailService.getEmailStatus(registrationId);
        return ResponseEntity.ok(ApiResponse.success(status, "Email status fetched successfully"));
    }

    /**
     * Get email status summary for all delegates of a form
     * 
     * @param formId - Form ID
     * @param authentication - Admin authentication
     * @return Summary report
     */
    @GetMapping("/status/form/{formId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmailStatusSummary(
            @PathVariable Long formId,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("Admin {} requested email status summary for form: {}", adminEmail, formId);

        Map<String, Object> summary = emailService.getEmailStatusSummary(formId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Email status summary fetched successfully"));
    }

    /**
     * Send emails to all pending delegates of a form (Bulk - Async)
     * 
     * @param formId - Form ID
     * @param authentication - Admin authentication
     * @return Summary of email sending
     */
    @PostMapping("/send-all/{formId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendEmailsToAllDelegates(
            @PathVariable Long formId,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        log.info("Admin {} requested bulk email send for form: {}", adminEmail, formId);

        // Get pending registrations
        Map<String, Object> summary = emailService.getEmailStatusSummary(formId);
        int pending = (int) summary.get("emailPending");
        
        if (pending == 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All emails already sent");
            response.put("pending", 0);
            return ResponseEntity.ok(ApiResponse.success(response, "No pending emails to send"));
        }

        // Send all pending emails asynchronously
        int queued = 0;
        int failed = 0;
        
        // Get pending delegates from summary
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> pendingDelegates = 
            (java.util.List<Map<String, Object>>) summary.get("pendingDelegates");
        
        for (Map<String, Object> delegate : pendingDelegates) {
            String registrationId = (String) delegate.get("registrationId");
            try {
                asyncEmailService.sendEmailAsync(registrationId);
                queued++; 
                log.info("Email queued for registration: {}", registrationId);
            } catch (Exception e) {
                failed++;
                log.error("Failed to queue email for registration {}: {}", registrationId, e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalPending", pending);
        result.put("queued", queued);
        result.put("failed", failed);
        result.put("message", "Bulk email queued: " + queued + " queued, " + failed + " failed");

        return ResponseEntity.ok(ApiResponse.success(result, "Bulk email queued successfully"));
    }
}