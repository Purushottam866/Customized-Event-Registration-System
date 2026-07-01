package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.BadgePrint;
import com.eventregistration.event_registration_system.entity.Registration;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgePrintRepository extends JpaRepository<BadgePrint, Long> {
    
    // For Event Module
    List<BadgePrint> findByRegistration(Registration registration);
    long countByRegistration(Registration registration);
    
    // For Simple Module
    List<BadgePrint> findBySimpleRegistration(SimpleRegistration simpleRegistration);
    long countBySimpleRegistration(SimpleRegistration simpleRegistration);
    
    // Total counts
    @Query("SELECT COUNT(bp) FROM BadgePrint bp WHERE bp.moduleType = :moduleType")
    long countByModuleType(@Param("moduleType") String moduleType);
    
    @Query("SELECT COUNT(bp) FROM BadgePrint bp WHERE bp.simpleRegistration.form.id = :formId AND bp.moduleType = 'SIMPLE'")
    long countByFormId(@Param("formId") Long formId);
}