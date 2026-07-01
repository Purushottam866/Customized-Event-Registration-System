package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.service.SimpleExcelImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/simple/registrations")
@RequiredArgsConstructor
@Slf4j
public class SimpleExcelController {

    private final SimpleExcelImportService excelImportService;

    @PostMapping("/import-excel/{formId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importExcel(
            @PathVariable Long formId,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> result = excelImportService.importRegistrations(formId, file);
        log.info("Excel import completed for form: {}", formId);
        return ResponseEntity.ok(ApiResponse.success(result, "Excel import completed successfully"));
    }

    @GetMapping("/template/{formId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable Long formId) {
        byte[] template = excelImportService.generateSampleTemplate(formId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registration_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(template);
    }
}