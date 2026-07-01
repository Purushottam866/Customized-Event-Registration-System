package com.eventregistration.event_registration_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow all endpoints
                        .allowedOriginPatterns("*") // Use allowedOriginPatterns instead of allowedOrigins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Allowed HTTP methods
                        .allowedHeaders("*") // Allowed headers
                        .exposedHeaders("Authorization", "Content-Disposition") // Headers exposed to client
                        .allowCredentials(true) // Allow cookies and credentials
                        .maxAge(3600); // Cache preflight requests for 1 hour
            }
        };
    }
}