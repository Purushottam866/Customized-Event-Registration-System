package com.eventregistration.event_registration_system.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", unique = true, nullable = false, length = 20)
    private String registrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdBy", "createdAt"})
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_template_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FormTemplate formTemplate;

    @Column(columnDefinition = "JSON", nullable = false)
    private String formData;

    @Column(name = "qr_code", columnDefinition = "LONGTEXT")  // Changed to LONGTEXT
    private String qrCode;

    @Column(name = "check_in_status")
    private Boolean checkInStatus = false;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}