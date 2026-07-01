package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.entity.SimpleBadgePrint;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import com.eventregistration.event_registration_system.service.SimpleBadgePrintService;
import com.eventregistration.event_registration_system.service.SimpleBadgeService;
import com.eventregistration.event_registration_system.service.SimpleFormService;
import com.eventregistration.event_registration_system.service.SimpleRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple/badge")
@RequiredArgsConstructor
@Slf4j
public class SimpleBadgeController {

    private final SimpleBadgeService badgeService;
    private final SimpleBadgePrintService badgePrintService;
    private final SimpleRegistrationService registrationService;
    private final SimpleFormService formService;

    @PostMapping(value = "/generate/{registrationId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generateBadge(
            @PathVariable String registrationId,
            @RequestBody(required = false) List<String> selectedFields,
            Authentication authentication) {
        
        String adminEmail = authentication.getName();
        SimpleRegistration registration = registrationService.getRegistrationById(registrationId);
        Long formId = registration.getForm().getId();
        
        // Get default badge fields from form
        List<String> defaultFields = formService.getBadgeFields(formId);
        
        // Use request fields if provided, otherwise use form defaults
        List<String> fields = (selectedFields != null && !selectedFields.isEmpty())
                ? selectedFields
                : defaultFields;
        
        // Log the badge print
        Long adminId = 1L; // TODO: Get actual admin ID
        badgePrintService.logBadgePrint(registrationId, fields, adminId);
        
        String html = badgeService.generateBadgeHTML(registration, fields);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping(value = "/generate-bulk", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> generateBulkBadges(
            @RequestBody List<Long> registrationIds,
            @RequestParam(required = false) Long formId,
            Authentication authentication) {
        
        if (registrationIds == null || registrationIds.isEmpty()) {
            throw new IllegalArgumentException("No registrations selected");
        }
        
        if (formId == null) {
            SimpleRegistration first = registrationService.getRegistrationByLongId(registrationIds.get(0));
            formId = first.getForm().getId();
        }
        
        List<String> defaultFields = formService.getBadgeFields(formId);
        String html = badgeService.generateBulkBadgeHTML(registrationIds, defaultFields);
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/print-count/{registrationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPrintCount(@PathVariable String registrationId) {
        long count = badgePrintService.getPrintCount(registrationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("registrationId", registrationId);
        response.put("printCount", count);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Print count fetched successfully"));
    }

    @GetMapping("/total-print-count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalPrintCount() {
        long totalPrints = badgePrintService.getTotalPrintCount();
        long totalRegistrations = badgePrintService.getTotalRegistrationsCount();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalPrints", totalPrints);
        response.put("totalRegistrations", totalRegistrations);
        response.put("averagePrintsPerRegistration", 
            totalRegistrations > 0 ? Math.round((double) totalPrints / totalRegistrations * 100.0) / 100.0 : 0);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Total print count fetched successfully"));
    }

    @GetMapping("/total-print-count/form/{formId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalPrintCountByForm(@PathVariable Long formId) {
        long totalPrints = badgePrintService.getTotalPrintCountByForm(formId);
        long totalRegistrations = badgePrintService.getTotalRegistrationsCountByForm(formId);
        String formTitle = formService.getFormById(formId).getTitle();
        
        Map<String, Object> response = new HashMap<>();
        response.put("formId", formId);
        response.put("formTitle", formTitle);
        response.put("totalPrints", totalPrints);
        response.put("totalRegistrations", totalRegistrations);
        response.put("averagePrintsPerRegistration", 
            totalRegistrations > 0 ? Math.round((double) totalPrints / totalRegistrations * 100.0) / 100.0 : 0);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Total print count for form fetched successfully"));
    }

    @GetMapping("/print-history/{registrationId}")
    public ResponseEntity<ApiResponse<List<SimpleBadgePrint>>> getPrintHistory(@PathVariable String registrationId) {
        List<SimpleBadgePrint> history = badgePrintService.getPrintHistory(registrationId);
        return ResponseEntity.ok(ApiResponse.success(history, "Print history fetched successfully"));
    }

    // ==================== EXPORT TO EXCEL ====================

    @GetMapping("/export/all")
    public ResponseEntity<byte[]> exportAllBadgePrints() {
        List<Map<String, Object>> exportData = badgePrintService.getAllBadgePrintsForExport();
        byte[] excelBytes = generateExcelExport(exportData, "All Badge Prints");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=badge_prints_all.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    @GetMapping("/export/form/{formId}")
    public ResponseEntity<byte[]> exportBadgePrintsByForm(@PathVariable Long formId) {
        List<Map<String, Object>> exportData = badgePrintService.getBadgePrintsForExportByForm(formId);
        String formTitle = formService.getFormById(formId).getTitle();
        String fileName = "badge_prints_" + formTitle.replaceAll(" ", "_") + ".xlsx";
        
        byte[] excelBytes = generateExcelExport(exportData, "Badge Prints - " + formTitle);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }

    // ==================== HELPER METHOD ====================

    private byte[] generateExcelExport(List<Map<String, Object>> exportData, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Registration ID", "Name", "Email", "Mobile", "Company", 
                "Designation", "City", "Printed At", "Printed By", "Selected Fields"
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            
            // Data rows
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Map<String, Object> data : exportData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) data.getOrDefault("registrationId", "N/A"));
                row.createCell(1).setCellValue((String) data.getOrDefault("name", "N/A"));
                row.createCell(2).setCellValue((String) data.getOrDefault("email", "N/A"));
                row.createCell(3).setCellValue((String) data.getOrDefault("mobile", "N/A"));
                row.createCell(4).setCellValue((String) data.getOrDefault("company", "N/A"));
                row.createCell(5).setCellValue((String) data.getOrDefault("designation", "N/A"));
                row.createCell(6).setCellValue((String) data.getOrDefault("city", "N/A"));
                
                // Printed At
                Object printedAt = data.get("printedAt");
                if (printedAt != null) {
                    row.createCell(7).setCellValue(printedAt.toString());
                } else {
                    row.createCell(7).setCellValue("N/A");
                }
                
                row.createCell(8).setCellValue(String.valueOf(data.getOrDefault("printedBy", "N/A")));
                row.createCell(9).setCellValue((String) data.getOrDefault("selectedFields", "N/A"));
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width
                if (sheet.getColumnWidth(i) < 4000) { 
                    sheet.setColumnWidth(i, 4000);
                }
            }
            
            // Write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating Excel export: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }
}