package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.SimpleBadgePrint;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimpleBadgePrintRepository extends JpaRepository<SimpleBadgePrint, Long> {
    
    List<SimpleBadgePrint> findByRegistration(SimpleRegistration registration);
    
    long countByRegistration(SimpleRegistration registration);
    
    @Query("SELECT COUNT(bp) FROM SimpleBadgePrint bp WHERE bp.registration.form.id = :formId")
    long countByFormId(@Param("formId") Long formId);

    // NEW: For export - get all with registration data
    @Query("SELECT bp FROM SimpleBadgePrint bp LEFT JOIN FETCH bp.registration ORDER BY bp.printedAt DESC")
    List<SimpleBadgePrint> findAllByOrderByPrintedAtDesc();

    // NEW: For export by form
    @Query("SELECT bp FROM SimpleBadgePrint bp LEFT JOIN FETCH bp.registration r WHERE r.form.id = :formId ORDER BY bp.printedAt DESC")
    List<SimpleBadgePrint> findByFormIdOrderByPrintedAtDesc(@Param("formId") Long formId);
}