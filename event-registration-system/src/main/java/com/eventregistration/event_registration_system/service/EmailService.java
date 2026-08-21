package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.repository.SimpleRegistrationRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import com.eventregistration.event_registration_system.util.QRCodeGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SimpleRegistrationRepository registrationRepository;
    private final JsonConverter jsonConverter;
    private final QRCodeGenerator qrCodeGenerator;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.organization}")
    private String organizationName;

    @Transactional
    public boolean sendRegistrationEmail(String registrationId) {
        try {
            SimpleRegistration registration = registrationRepository.findByRegistrationId(registrationId)
                    .orElseThrow(() -> new RuntimeException("Registration not found: " + registrationId));

            Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
            String eventName = registration.getForm().getTitle();
            String name = getValue(formData, "full name", "name", "fullname", "full Name");
            String company = getValue(formData, "company name", "company", "companyname", "Company Name");

            String badgeImage = registration.getQrCode();
            log.info("QR Code length: {}", badgeImage != null ? badgeImage.length() : 0);

            String subject = "Registration Successful - " + eventName;
            String htmlContent = buildEmailContent(name, eventName, company, registration.getRegistrationId());

            sendHtmlEmail(getEmail(formData), subject, htmlContent, badgeImage);

            registration.setEmailSent(true);
            registration.setEmailSentAt(LocalDateTime.now());
            registration.setEmailRetryCount(0);
            registrationRepository.save(registration);

            log.info("Registration email sent successfully to: {} ({})", name, registrationId);
            return true;

        } catch (Exception e) {
            log.error("Failed to send registration email for {}: {}", registrationId, e.getMessage());
            updateEmailFailure(registrationId);
            return false;
        }
    }

    @Transactional
    public boolean resendRegistrationEmail(String registrationId) {
        try {
            SimpleRegistration registration = registrationRepository.findByRegistrationId(registrationId)
                    .orElseThrow(() -> new RuntimeException("Registration not found: " + registrationId));

            Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
            String eventName = registration.getForm().getTitle();
            String name = getValue(formData, "full name", "name", "fullname", "full Name");
            String company = getValue(formData, "company name", "company", "companyname", "Company Name");

            String badgeImage = registration.getQrCode();
            log.info("QR Code length for resend: {}", badgeImage != null ? badgeImage.length() : 0);

            String subject = "Registration Successful - " + eventName;
            String htmlContent = buildEmailContent(name, eventName, company, registration.getRegistrationId());

            sendHtmlEmail(getEmail(formData), subject, htmlContent, badgeImage);

            registration.setEmailSent(true);
            registration.setEmailSentAt(LocalDateTime.now());
            registration.setEmailRetryCount(registration.getEmailRetryCount() != null ? registration.getEmailRetryCount() + 1 : 1);
            registrationRepository.save(registration);

            log.info("Registration email resent successfully to: {} ({}) - Retry count: {}", name, registrationId, registration.getEmailRetryCount());
            return true;

        } catch (Exception e) {
            log.error("Failed to resend registration email for {}: {}", registrationId, e.getMessage());
            updateEmailFailure(registrationId);
            return false;
        }
    }

    @Transactional
    public Map<String, Object> sendEmailsToAllDelegates(Long formId) {
        List<SimpleRegistration> registrations = registrationRepository.findByFormIdOrderByCreatedAtDesc(formId);
        
        int total = registrations.size();
        int sent = 0;
        int failed = 0;
        int alreadySent = 0;

        for (SimpleRegistration registration : registrations) {
            if (Boolean.TRUE.equals(registration.getEmailSent())) {
                alreadySent++;
                continue;
            }
            boolean success = sendRegistrationEmail(registration.getRegistrationId());
            if (success) {
                sent++;
            } else {
                failed++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("sent", sent);
        result.put("failed", failed);
        result.put("alreadySent", alreadySent);
        result.put("message", "Email sending completed: " + sent + " sent, " + failed + " failed, " + alreadySent + " already sent");

        return result;
    }

    public Map<String, Object> getEmailStatus(String registrationId) {
        SimpleRegistration registration = registrationRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found: " + registrationId));

        Map<String, Object> status = new HashMap<>();
        status.put("registrationId", registration.getRegistrationId());
        status.put("emailSent", registration.getEmailSent());
        status.put("emailSentAt", registration.getEmailSentAt());
        status.put("emailRetryCount", registration.getEmailRetryCount() != null ? registration.getEmailRetryCount() : 0);

        return status;
    }

    public Map<String, Object> getEmailStatusSummary(Long formId) {
        List<SimpleRegistration> registrations = registrationRepository.findByFormIdOrderByCreatedAtDesc(formId);
        
        int total = registrations.size();
        int sent = 0;
        int pending = 0;
        List<Map<String, Object>> pendingDelegates = new ArrayList<>();

        for (SimpleRegistration registration : registrations) {
            Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
            String name = getValue(formData, "full name", "name", "fullname", "full Name");
            String email = getValue(formData, "email address", "email", "emailaddress", "Email Address");
            
            if (Boolean.TRUE.equals(registration.getEmailSent())) {
                sent++;
            } else {
                pending++;
                Map<String, Object> delegateInfo = new HashMap<>();
                delegateInfo.put("registrationId", registration.getRegistrationId());
                delegateInfo.put("name", name);
                delegateInfo.put("email", email);
                delegateInfo.put("createdAt", registration.getCreatedAt());
                pendingDelegates.add(delegateInfo);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("formId", formId);
        summary.put("totalDelegates", total);
        summary.put("emailSent", sent);
        summary.put("emailPending", pending);
        summary.put("pendingPercentage", total > 0 ? Math.round((double) pending / total * 100) : 0);
        summary.put("pendingDelegates", pendingDelegates);

        return summary;
    }

    // ==================== HELPER METHODS ====================

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent, String qrCodeBase64) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // Add QR code as inline attachment if present
        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(qrCodeBase64);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            helper.addInline("qrCode", resource, "image/png");
            log.info("QR code attached to email as inline image");
        }

        mailSender.send(message);
        log.info("Email sent to: {}", toEmail);
    }

    /**
     * Build email HTML content - uses CID reference for QR code
     */
    private String buildEmailContent(String name, String eventName, String company, String registrationId) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background: #f4f4f4; }\n");
        html.append("        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }\n");
        html.append("        .header { background: #0066cc; color: white; padding: 25px 20px; text-align: center; }\n");
        html.append("        .header h2 { margin: 0; font-size: 24px; }\n");
        html.append("        .content { padding: 30px; }\n");
        html.append("        .content p { font-size: 15px; line-height: 1.6; color: #333; }\n");
        html.append("        .badge-container { text-align: center; background: #f8f9fa; padding: 20px; border-radius: 10px; border: 2px solid #333; margin: 20px 0; }\n");
        html.append("        .badge-container img { max-width: 150px; display: block; margin: 0 auto; }\n");
        html.append("        .badge-container .name { font-size: 22px; font-weight: bold; margin: 10px 0 5px; text-transform: uppercase; }\n");
        html.append("        .badge-container .company { font-size: 16px; color: #555; text-transform: uppercase; }\n");
        html.append("        .reg-id { text-align: center; margin: 15px 0; font-size: 14px; color: #999; }\n");
        html.append("        .footer { text-align: center; padding: 20px; background: #f4f4f4; font-size: 12px; color: #999; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h2>Registration Successful!</h2>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"content\">\n");
        html.append("            <p>Dear <strong>").append(name).append("</strong>,</p>\n");
        html.append("            <p>Your registration for <strong>").append(eventName).append("</strong> is successful!</p>\n");
        html.append("            <p>Please show this QR code at the registration counter:</p>\n");
        html.append("            <div class=\"badge-container\">\n");
        html.append("                <img src=\"cid:qrCode\" alt=\"QR Code\" style=\"max-width:150px; display:block; margin:0 auto;\"/>\n");
        html.append("                <div class=\"name\">").append(name.toUpperCase()).append("</div>\n");
        html.append("                <div class=\"company\">").append(company != null ? company.toUpperCase() : "").append("</div>\n");
        html.append("            </div>\n");
        html.append("            <p><strong>Registration ID:</strong> ").append(registrationId).append("</p>\n");
        html.append("            <p>Thank you for registering!</p>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"footer\">\n");
        html.append("            <p>&copy; ").append(java.time.Year.now().getValue()).append(" ").append(organizationName).append(". All rights reserved.</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    private String getValue(Map<String, Object> formData, String... keys) {
        for (String key : keys) {
            Object value = formData.get(key);
            if (value != null && !value.toString().isEmpty()) {
                return value.toString();
            }
        }
        return "";
    }

    private String getEmail(Map<String, Object> formData) {
        String email = getValue(formData, "email address", "email", "emailaddress", "Email Address");
        if (email.isEmpty()) {
            throw new RuntimeException("Email not found in registration data");
        }
        return email;
    }

    private void updateEmailFailure(String registrationId) {
        try {
            SimpleRegistration registration = registrationRepository.findByRegistrationId(registrationId).orElse(null);
            if (registration != null) {
                registration.setEmailRetryCount(registration.getEmailRetryCount() != null ? registration.getEmailRetryCount() + 1 : 1);
                registrationRepository.save(registration);
            }
        } catch (Exception e) {
            log.error("Failed to update email failure status for {}: {}", registrationId, e.getMessage());
        }
    }
}