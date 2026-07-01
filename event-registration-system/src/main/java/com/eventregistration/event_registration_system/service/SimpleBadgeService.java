package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.util.BadgeHTMLGenerator;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleBadgeService {

    private final SimpleRegistrationService registrationService;
    private final BadgeHTMLGenerator badgeHTMLGenerator;
    private final JsonConverter jsonConverter;

    public String generateBadgeHTML(SimpleRegistration registration, List<String> selectedFields) {
        Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
        String eventName = registration.getForm().getTitle();
        String qrCodeBase64 = registration.getQrCode();
        
        return generateSimpleBadgeHTML(registration, selectedFields, eventName, qrCodeBase64, formData);
    }

    public String generateBulkBadgeHTML(List<Long> registrationIds, List<String> selectedFields) {
        List<SimpleRegistration> registrations = registrationIds.stream()
                .map(registrationService::getRegistrationByLongId)
                .toList();
        
        if (registrations.isEmpty()) {
            throw new IllegalArgumentException("No registrations found");
        }
        
        String eventName = registrations.get(0).getForm().getTitle();
        
        return generateSimpleBulkBadgeHTML(registrations, selectedFields, eventName);
    }

    private String generateSimpleBadgeHTML(SimpleRegistration registration, 
                                           List<String> selectedFields,
                                           String eventName,
                                           String qrCodeBase64,
                                           Map<String, Object> formData) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Badge - ").append(registration.getRegistrationId()).append("</title>\n");
        html.append("    <style>\n");
        html.append(getBadgeCSS());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Font controls
        html.append("    <div class=\"font-controls\">\n");
        html.append("        <label>Font Size:</label>\n");
        html.append("        <button onclick=\"changeFontSize(-1)\">A-</button>\n");
        html.append("        <span id=\"fontSizeDisplay\">16</span>px\n");
        html.append("        <button onclick=\"changeFontSize(1)\">A+</button>\n");
        html.append("        <span style=\"margin-left:20px;font-size:12px;color:#999;\">(Changes apply to badge only, not saved)</span>\n");
        html.append("    </div>\n");
        
        html.append("    <div class=\"badge-container\" id=\"badgeContainer\">\n");
        html.append("        <div class=\"badge\">\n");
        
        // QR Code
        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            html.append("            <div class=\"qr-section\">\n");
            html.append("                <img id=\"qrImage\" src=\"data:image/png;base64,").append(qrCodeBase64).append("\" alt=\"QR Code\"/>\n");
            html.append("            </div>\n");
        }
        
        // Selected Fields
        html.append("            <div class=\"fields-section\">\n");
        for (String field : selectedFields) {
            Object value = formData.get(field.toLowerCase());
            if (value != null && !value.toString().isEmpty()) {
                html.append("                <div class=\"field-value\">").append(value.toString().toUpperCase()).append("</div>\n");
            }
        }
        html.append("            </div>\n");
        
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        html.append("    <div class=\"print-controls\">\n");
        html.append("        <button onclick=\"window.print()\">🖨️ Print Badge</button>\n");
        html.append("    </div>\n");
        
        // JavaScript
        html.append("    <script>\n");
        html.append("        let currentFontSize = 16;\n");
        html.append("        const minSize = 10;\n");
        html.append("        const maxSize = 40;\n\n");
        html.append("        function changeFontSize(delta) {\n");
        html.append("            currentFontSize = Math.min(maxSize, Math.max(minSize, currentFontSize + delta));\n");
        html.append("            document.getElementById('fontSizeDisplay').textContent = currentFontSize;\n");
        html.append("            const fields = document.querySelectorAll('.field-value');\n");
        html.append("            fields.forEach(field => {\n");
        html.append("                field.style.fontSize = currentFontSize + 'px';\n");
        html.append("            });\n");
        html.append("            const qrSize = Math.min(200, Math.max(100, currentFontSize * 6));\n");
        html.append("            const qrImage = document.getElementById('qrImage');\n");
        html.append("            if (qrImage) {\n");
        html.append("                qrImage.style.width = qrSize + 'px';\n");
        html.append("                qrImage.style.height = qrSize + 'px';\n");
        html.append("            }\n");
        html.append("        }\n");
        html.append("    </script>\n");
        
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    private String generateSimpleBulkBadgeHTML(List<SimpleRegistration> registrations,
                                               List<String> selectedFields,
                                               String eventName) {
        // Use the existing BadgeHTMLGenerator for bulk
        return badgeHTMLGenerator.generateBulkBadgeHTML(
            convertToRegistrations(registrations), 
            selectedFields, 
            eventName
        );
    }

    private String getBadgeCSS() {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                background: #f0f0f0;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                font-family: Arial, sans-serif;
                padding: 20px;
            }

            .font-controls {
                background: white;
                padding: 12px 25px;
                border-radius: 8px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 12px;
                flex-wrap: wrap;
                justify-content: center;
            }

            .font-controls label {
                font-weight: bold;
                font-size: 14px;
                color: #333;
            }

            .font-controls button {
                padding: 6px 18px;
                font-size: 18px;
                font-weight: bold;
                background: #0066cc;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                transition: background 0.2s;
                min-width: 40px;
            }

            .font-controls button:hover {
                background: #0055aa;
            }

            .font-controls button:active {
                transform: scale(0.95);
            }

            .font-controls span {
                font-weight: bold;
                font-size: 16px;
                min-width: 35px;
                text-align: center;
                color: #333;
            }

            .badge-container {
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 10px;
            }

            .badge {
                width: 280px;
                padding: 25px 20px;
                background: white;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                text-align: center;
            }

            .qr-section {
                text-align: center;
                margin-bottom: 18px;
            }

            .qr-section img {
                width: 120px;
                height: 120px;
                padding: 0;
                display: block;
                margin: 0 auto;
                transition: 0.3s;
            }

            .fields-section {
                text-align: center;
                margin: 5px 0;
            }

            .field-value {
                font-size: 16px;
                font-weight: 500;
                color: #000;
                padding: 4px 0;
                line-height: 1.4;
                text-transform: uppercase;
                letter-spacing: 0.5px;
                word-wrap: break-word;
                overflow-wrap: break-word;
                white-space: normal;
                max-width: 100%;
            }

            .field-value:first-child {
                font-size: 20px;
                font-weight: bold;
                letter-spacing: 1px;
            }

            .print-controls {
                margin-top: 25px;
                text-align: center;
            }

            .print-controls button {
                padding: 12px 40px;
                font-size: 16px;
                font-weight: bold;
                background: #28a745;
                color: white;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                transition: background 0.2s;
            }

            .print-controls button:hover {
                background: #218838;
            }

            .print-controls button:active {
                transform: scale(0.97);
            }

            @media print {
                body {
                    background: white;
                    padding: 0;
                }

                .font-controls {
                    display: none !important;
                }

                .badge-container {
                    padding: 0;
                }

                .badge {
                    box-shadow: none;
                    page-break-inside: avoid;
                    border: none;
                    border-radius: 0;
                    padding: 10px;
                }

                .print-controls {
                    display: none !important;
                }
            }
            """;
    }

    private List<com.eventregistration.event_registration_system.entity.Registration> convertToRegistrations(
            List<SimpleRegistration> simpleRegistrations) {
        return simpleRegistrations.stream()
                .map(simple -> {
                    com.eventregistration.event_registration_system.entity.Registration reg = 
                        new com.eventregistration.event_registration_system.entity.Registration();
                    reg.setRegistrationId(simple.getRegistrationId());
                    reg.setFormData(simple.getFormData());
                    reg.setQrCode(simple.getQrCode());
                    return reg;
                })
                .toList();
    }
}