package com.eventregistration.event_registration_system.config;

import com.eventregistration.event_registration_system.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ========== PUBLIC ENDPOINTS (NO JWT REQUIRED) ==========
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/simple/forms/published").permitAll()
                .requestMatchers("/api/simple/registrations/form/**").permitAll()
                
                // ========== ADMIN/AUTHENTICATED ENDPOINTS (JWT REQUIRED) ==========
                .requestMatchers("/api/events/**").authenticated()
                .requestMatchers("/api/registrations/**").authenticated()
                .requestMatchers("/api/checkin/**").authenticated()
                .requestMatchers("/api/badge/**").authenticated()
                .requestMatchers("/api/excel/**").authenticated()
                .requestMatchers("/api/simple/forms/**").authenticated()
                .requestMatchers("/api/simple/onspot/**").authenticated()
                .requestMatchers("/api/simple/badge/**").authenticated()
                .requestMatchers("/api/simple/registrations/import-excel/**").authenticated()
                .requestMatchers("/api/simple/registrations/template/**").authenticated()
                .requestMatchers("/api/simple/registrations/search/**").authenticated()
                .requestMatchers("/api/simple/registrations/count/**").authenticated()
                .requestMatchers("/api/simple/registrations/export/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}