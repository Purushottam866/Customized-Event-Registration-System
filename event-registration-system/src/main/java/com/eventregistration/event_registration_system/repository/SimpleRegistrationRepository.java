package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.SimpleForm;
import com.eventregistration.event_registration_system.entity.SimpleRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimpleRegistrationRepository extends JpaRepository<SimpleRegistration, Long> {
    
    Optional<SimpleRegistration> findByRegistrationId(String registrationId);
    
    List<SimpleRegistration> findByForm(SimpleForm form);
    
    List<SimpleRegistration> findByFormOrderByCreatedAtDesc(SimpleForm form);
    
    @Query(value = "SELECT * FROM simple_registrations sr WHERE sr.form_id = :formId AND " +
    	       "JSON_SEARCH(LOWER(sr.form_data), 'all', LOWER(CONCAT('%', :searchTerm, '%'))) IS NOT NULL",
    	       nativeQuery = true)
    	List<SimpleRegistration> searchRegistrations(@Param("formId") Long formId, 
    	                                              @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(sr) FROM SimpleRegistration sr WHERE sr.form.id = :formId")
    long countByFormId(@Param("formId") Long formId);
}