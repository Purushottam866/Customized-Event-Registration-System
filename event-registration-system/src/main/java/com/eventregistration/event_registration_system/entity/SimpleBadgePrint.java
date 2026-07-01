package com.eventregistration.event_registration_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "simple_badge_prints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleBadgePrint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private SimpleRegistration registration;

    @Column(name = "printed_by")
    private Long printedBy;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "selected_fields", columnDefinition = "JSON")
    private String selectedFields;

    @PrePersist
    protected void onCreate() {
        printedAt = LocalDateTime.now();
    }
}