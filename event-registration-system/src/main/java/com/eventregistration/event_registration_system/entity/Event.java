package com.eventregistration.event_registration_system.entity;

import com.eventregistration.event_registration_system.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"password", "createdAt", "isActive"})
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @Column(name = "event_logo_url")
    private String eventLogoUrl;

    // NEW: Badge fields configuration (JSON array of field names to print)
    @Column(name = "badge_fields", columnDefinition = "JSON")
    private String badgeFields;  // Default: ["Name", "Company"]

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Set default badge fields if not set
        if (badgeFields == null) {
            badgeFields = "[\"Name\", \"Company\"]";
        }
    }
}