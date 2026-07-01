package com.eventregistration.event_registration_system.exception;

import com.eventregistration.event_registration_system.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex, 
                                                                     HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.notFound(
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex,
                                                               HttpServletRequest request) {
        log.error("Bad request: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.badRequest(
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateRegistration(DuplicateRegistrationException ex,
                                                                          HttpServletRequest request) {
        log.error("Duplicate registration: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.conflict(
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex,
                                                                   HttpServletRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.unauthorized(
            "Invalid username or password",
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation failed: {}", errors);
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .statusCode(400)
                .message("Validation failed")
                .data(errors)
                .errorCode("VALIDATION_ERROR")
                .path(request.getRequestURI())
                .timestamp(System.currentTimeMillis())
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex,
                                                                     HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.internalError(
            "An unexpected error occurred: " + ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}