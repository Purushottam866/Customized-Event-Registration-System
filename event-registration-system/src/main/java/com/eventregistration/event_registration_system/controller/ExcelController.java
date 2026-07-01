package com.eventregistration.event_registration_system.controller;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import com.eventregistration.event_registration_system.service.ExcelImportService;
import com.eventregistration.event_registration_system.service.FormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@Slf4j
public class ExcelController {

    private final ExcelImportService excelImportService;
    private final FormService formService;

    @PostMapping("/import/{eventId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importRegistrations(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> result = excelImportService.importRegistrations(eventId, file);
        log.info("Excel import completed for event: {}", eventId);
        return ResponseEntity.ok(ApiResponse.success(result, "Excel import completed successfully"));
    }

    @GetMapping("/template/{eventId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable Long eventId) {
        List<Map<String, Object>> formFields = formService.getFormFields(eventId);
        byte[] template = excelImportService.generateSampleTemplate(formFields);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registration_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(template);
    }
}