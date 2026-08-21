package com.eventregistration.event_registration_system.util;

import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeHTMLGenerator {

    private final JsonConverter jsonConverter;
    private final QRCodeGenerator qrCodeGenerator;

    /**
     * Generate HTML for single badge - Clean version with font size control
     */
    public String generateSimpleBadgeHTML(SimpleRegistration registration, 
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

    /**
     * Generate HTML for bulk badges
     */
    public String generateSimpleBulkBadgeHTML(List<SimpleRegistration> registrations,
                                              List<String> selectedFields,
                                              String eventName) {
        StringBuilder html = new StringBuilder(1024 * 1024 * 5);
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Bulk Badges</title>\n");
        html.append("    <style>\n");
        html.append(getBulkBadgeCSS());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Font size controls
        html.append("    <div class=\"font-controls\">\n");
        html.append("        <label>Font Size:</label>\n");
        html.append("        <button onclick=\"changeFontSize(-1)\">A-</button>\n");
        html.append("        <span id=\"fontSizeDisplay\">13</span>px\n");
        html.append("        <button onclick=\"changeFontSize(1)\">A+</button>\n");
        html.append("        <span style=\"margin-left:20px;font-size:12px;color:#999;\">(Changes apply to all badges, not saved)</span>\n");
        html.append("    </div>\n");
        
        html.append("    <div class=\"bulk-container\" id=\"badgeContainer\">\n");
        
        for (SimpleRegistration registration : registrations) {
            Map<String, Object> formData = jsonConverter.fromJsonToMap(registration.getFormData());
            String qrCodeBase64 = registration.getQrCode();
            
            html.append("        <div class=\"badge\">\n");
            
            // QR Code
            if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
                html.append("            <div class=\"qr-section\">\n");
                html.append("                <img class=\"qr-image\" src=\"data:image/png;base64,");
                html.append(qrCodeBase64);
                html.append("\" alt=\"QR Code\"/>\n");
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
        }
        
        html.append("    </div>\n");
        
        html.append("    <div class=\"print-controls\">\n");
        html.append("        <button onclick=\"window.print()\">🖨️ Print All Badges</button>\n");
        html.append("    </div>\n");
        
        // JavaScript
        html.append("    <script>\n");
        html.append("        let currentFontSize = 13;\n");
        html.append("        const minSize = 8;\n");
        html.append("        const maxSize = 30;\n\n");
        html.append("        function changeFontSize(delta) {\n");
        html.append("            currentFontSize = Math.min(maxSize, Math.max(minSize, currentFontSize + delta));\n");
        html.append("            document.getElementById('fontSizeDisplay').textContent = currentFontSize;\n");
        html.append("            const fields = document.querySelectorAll('.field-value');\n");
        html.append("            fields.forEach(field => {\n");
        html.append("                field.style.fontSize = currentFontSize + 'px';\n");
        html.append("            });\n");
        html.append("            const qrSize = Math.min(140, Math.max(70, currentFontSize * 5));\n");
        html.append("            const qrImages = document.querySelectorAll('.qr-image');\n");
        html.append("            qrImages.forEach(img => {\n");
        html.append("                img.style.width = qrSize + 'px';\n");
        html.append("                img.style.height = qrSize + 'px';\n");
        html.append("            });\n");
        html.append("        }\n");
        html.append("    </script>\n");
        
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * CSS for single badge
     */
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

    /**
     * CSS for bulk badges
     */
    private String getBulkBadgeCSS() {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                background: #f0f0f0;
                font-family: Arial, sans-serif;
                padding: 20px;
                display: flex;
                flex-direction: column;
                align-items: center;
            }

            .font-controls {
                background: white;
                padding: 10px 20px;
                border-radius: 8px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
                flex-wrap: wrap;
                justify-content: center;
            }

            .font-controls label {
                font-weight: bold;
                font-size: 14px;
                color: #333;
            }

            .font-controls button {
                padding: 5px 15px;
                font-size: 18px;
                font-weight: bold;
                background: #0066cc;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                transition: background 0.2s;
                min-width: 35px;
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
                min-width: 30px;
                text-align: center;
                color: #333;
            }

            .bulk-container {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 20px;
                max-width: 1200px;
                margin: 0 auto;
                width: 100%;
            }

            .badge {
                width: 100%;
                padding: 15px 12px;
                background: white;
                border-radius: 8px;
                text-align: center;
                page-break-inside: avoid;
                break-inside: avoid;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            }

            .qr-section {
                text-align: center;
                margin-bottom: 12px;
            }

            .qr-section img {
                width: 80px;
                height: 80px;
                padding: 0;
                display: block;
                margin: 0 auto;
                transition: 0.3s;
            }

            .fields-section {
                text-align: center;
                margin: 3px 0;
            }

            .field-value {
                font-size: 13px;
                font-weight: 500;
                color: #000;
                padding: 2px 0;
                line-height: 1.3;
                text-transform: uppercase;
                letter-spacing: 0.3px;
                word-wrap: break-word;
                overflow-wrap: break-word;
                white-space: normal;
                max-width: 100%;
            }

            .field-value:first-child {
                font-size: 17px;
                font-weight: bold;
                letter-spacing: 0.5px;
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

                .badge {
                    box-shadow: none;
                    page-break-inside: avoid;
                    break-inside: avoid;
                    border: none;
                    border-radius: 0;
                    padding: 10px 8px;
                }

                .print-controls {
                    display: none !important;
                }

                .bulk-container {
                    display: grid;
                    grid-template-columns: repeat(4, 1fr);
                    gap: 10px;
                    max-width: 100%;
                }
            }

            @media print and (max-width: 800px) {
                .bulk-container {
                    grid-template-columns: repeat(2, 1fr);
                }
            }

            @media (max-width: 768px) {
                .bulk-container {
                    grid-template-columns: repeat(2, 1fr);
                }
            }

            @media (max-width: 480px) {
                .bulk-container {
                    grid-template-columns: 1fr;
                }
            }
            """;
    }
}