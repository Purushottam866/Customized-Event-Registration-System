package com.eventregistration.event_registration_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "simple_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", unique = true, nullable = false, length = 20)
    private String registrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private SimpleForm form;

    @Column(columnDefinition = "JSON", nullable = false)
    private String formData;

    @Column(name = "qr_code", columnDefinition = "LONGTEXT")
    private String qrCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}