package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.entity.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    Optional<FormTemplate> findByEventAndIsActiveTrue(Event event);
    List<FormTemplate> findByEvent(Event event);
    Optional<FormTemplate> findByEventIdAndIsActiveTrue(Long eventId);
}