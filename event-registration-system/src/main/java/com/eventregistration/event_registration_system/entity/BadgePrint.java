package com.eventregistration.event_registration_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "badge_prints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgePrint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // For Event Module Registration - Made nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = true)
    private Registration registration;

    // For Simple Module Registration - Made nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simple_registration_id", nullable = true)
    private SimpleRegistration simpleRegistration;

    @Column(name = "printed_by")
    private Long printedBy;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "selected_fields", columnDefinition = "JSON")
    private String selectedFields;

    @Column(name = "module_type")
    private String moduleType; // "EVENT" or "SIMPLE"

    @PrePersist
    protected void onCreate() {
        printedAt = LocalDateTime.now();
    }
}