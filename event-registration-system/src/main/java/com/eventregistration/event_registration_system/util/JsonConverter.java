package com.eventregistration.event_registration_system.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class JsonConverter {

    private final ObjectMapper objectMapper;

    public JsonConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Convert object to JSON string
     */
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Convert JSON string to object
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object: {}", e.getMessage());
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * Convert JSON string to Map
     */
    public Map<String, Object> fromJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to Map: {}", e.getMessage());
            throw new RuntimeException("Failed to convert JSON to Map", e);
        }
    }

    /**
     * Convert Map to JSON string
     */
    public String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Error converting Map to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to convert Map to JSON", e);
        }
    }

    /**
     * Pretty print JSON
     */
    public String prettyPrint(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error pretty printing JSON: {}", e.getMessage());
            return json;
        }
    }
}