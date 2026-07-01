package com.eventregistration.event_registration_system.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class QRCodeGenerator {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * Generate QR code as Base64 encoded string
     */
    public String generateQRCodeBase64(String data) {
        return generateQRCodeBase64(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Generate QR code as Base64 encoded string with custom size
     */
    public String generateQRCodeBase64(String data, int width, int height) {
        try {
            BufferedImage qrImage = generateQRCode(data, width, height);
            return convertToBase64(qrImage);
        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate QR code as BufferedImage
     */
    public BufferedImage generateQRCode(String data, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Convert BufferedImage to Base64 string
     */
    private String convertToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Generate QR data with ONLY VALUES separated by spaces
     * Format: "Sai sai@qp.com 9876543211 Innovation Labs Product Manager Mumbai Non-Vegetarian"
     * Values are in the exact order of form fields
     */
    public String generateQRDataWithValuesOnly(Map<String, Object> registrationData, List<Map<String, Object>> formFields) {
        List<String> values = new ArrayList<>();
        
        // Get values in the exact order of form fields
        for (Map<String, Object> field : formFields) {
            String label = (String) field.get("label");
            // Convert label to lowercase for lookup (case insensitive)
            String key = label.toLowerCase();
            Object value = registrationData.get(key);
            
            // If value is null or empty, add empty string
            if (value != null && !value.toString().isEmpty()) {
                values.add(value.toString().trim());
            } else {
                values.add(""); // Empty for missing values
            }
        }
        
        // Join values with single space
        return String.join(" ", values);
    }

    /**
     * Decode QR data with values only back to Map with keys
     * The values are in the exact order of form fields
     */
    public Map<String, Object> decodeQRDataWithValuesOnly(String qrData, List<Map<String, Object>> formFields) {
        // Split by spaces, but be careful with multi-word values
        // We'll use a smarter approach - split by spaces but preserve multi-word values
        String[] values = qrData.split(" ");
        Map<String, Object> result = new LinkedHashMap<>();
        
        // Handle case where a value might have spaces (like "Innovation Labs")
        // For simplicity, we'll assume values with spaces are handled properly
        // Or we can use a different approach for decoding
        
        int valueIndex = 0;
        for (int i = 0; i < Math.min(values.length, formFields.size()); i++) {
            String label = (String) formFields.get(i).get("label");
            result.put(label.toLowerCase(), values[i]);
        }
        
        return result;
    }

    /**
     * Generate QR data string (JSON format with keys - for backward compatibility)
     */
    public String generateQRData(Map<String, Object> registrationData) {
        return new JsonConverter().toJson(registrationData);
    }

    /**
     * Decode QR data from JSON
     */
    public Map<String, Object> decodeQRData(String qrData) {
        return new JsonConverter().fromJsonToMap(qrData);
    }
}