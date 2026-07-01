package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.SimpleForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimpleFormRepository extends JpaRepository<SimpleForm, Long> {
    List<SimpleForm> findByIsPublishedTrue();
    Optional<SimpleForm> findByIdAndIsPublishedTrue(Long id);  
}