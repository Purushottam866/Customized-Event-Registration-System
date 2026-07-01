package com.eventregistration.event_registration_system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private int statusCode;
    private String message;
    private T data;
    private String errorCode;
    private String errorDetails;
    private String path;
    private Long timestamp;
    
    // Success response builder
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message(message != null ? message : "Success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(201)
                .message(message != null ? message : "Created successfully")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> created(T data) {
        return created(data, "Created successfully");
    }
    
    // Error response builder
    public static <T> ApiResponse<T> error(int statusCode, String message, String errorCode, String path) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> error(int statusCode, String message, String path) {
        return error(statusCode, message, null, path);
    }
    
    public static <T> ApiResponse<T> badRequest(String message, String path) {
        return error(400, message, "BAD_REQUEST", path);
    }
    
    public static <T> ApiResponse<T> notFound(String message, String path) {
        return error(404, message, "NOT_FOUND", path);
    }
    
    public static <T> ApiResponse<T> unauthorized(String message, String path) {
        return error(401, message, "UNAUTHORIZED", path);
    }
    
    public static <T> ApiResponse<T> forbidden(String message, String path) {
        return error(403, message, "FORBIDDEN", path);
    }
    
    public static <T> ApiResponse<T> conflict(String message, String path) {
        return error(409, message, "CONFLICT", path);
    }
    
    public static <T> ApiResponse<T> internalError(String message, String path) {
        return error(500, message, "INTERNAL_ERROR", path);
    }
}