package com.eventregistration.event_registration_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "simple_forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSON", nullable = false)
    private String fields;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "badge_fields", columnDefinition = "JSON")
    private String badgeFields;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (badgeFields == null) {
            badgeFields = "[\"Name\", \"Company\"]";
        }
    }
}