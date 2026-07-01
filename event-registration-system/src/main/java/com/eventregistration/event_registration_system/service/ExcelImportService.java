package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final RegistrationService registrationService;

    /**
     * Import registrations from Excel file
     */
    @Transactional
    public Map<String, Object> importRegistrations(Long eventId, MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successful = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            if (headerRow == null) {
                throw new BadRequestException("Excel file is empty or has no header row");
            }
            
            // Get headers (field names)
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }
            
            // Process each row (starting from row 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Map<String, Object> formData = new HashMap<>();
                    
                    // Map each cell to header
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        String header = headers.get(j);
                        Object value = getCellValue(cell);
                        
                        if (value != null && !value.toString().trim().isEmpty()) {
                            formData.put(header.toLowerCase(), value.toString().trim());
                        }
                    }
                    
                    // Register attendee
                    Registration registration = registrationService.registerAttendee(eventId, formData);
                    
                    Map<String, Object> successData = new HashMap<>();
                    successData.put("registrationId", registration.getRegistrationId());
                    successData.put("data", formData);
                    successful.add(successData);
                    
                } catch (Exception e) {
                    Map<String, Object> failedData = new HashMap<>();
                    failedData.put("row", i + 1);
                    failedData.put("error", e.getMessage());
                    failed.add(failedData);
                    log.warn("Failed to import row {}: {}", i + 1, e.getMessage());
                }
            }
            
            result.put("totalRows", sheet.getLastRowNum());
            result.put("successful", successful.size());
            result.put("failed", failed.size());
            result.put("successList", successful);
            result.put("failedList", failed);
            
            log.info("Excel import completed: {} successful, {} failed", successful.size(), failed.size());
            
        } catch (Exception e) {
            log.error("Error importing Excel: {}", e.getMessage());
            throw new BadRequestException("Failed to import Excel: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Get cell value based on cell type
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * Download sample Excel template
     */
    public byte[] generateSampleTemplate(List<Map<String, Object>> formFields) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registrations");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            for (Map<String, Object> field : formFields) {
                String label = (String) field.get("label");
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(label);
            }
            
            // Add sample data row
            Row sampleRow = sheet.createRow(1);
            for (int i = 0; i < formFields.size(); i++) {
                Map<String, Object> field = formFields.get(i);
                String type = (String) field.get("type");
                Cell cell = sampleRow.createCell(i);
                
                switch (type.toUpperCase()) {
                    case "EMAIL" -> cell.setCellValue("example@email.com");
                    case "PHONE" -> cell.setCellValue("9876543210");
                    case "DATE" -> cell.setCellValue("2024-12-31");
                    case "NUMBER" -> cell.setCellValue(100);
                    default -> cell.setCellValue("Sample Data");
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < formFields.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating sample template: {}", e.getMessage());
            throw new RuntimeException("Failed to generate sample template", e);
        }
    }
}