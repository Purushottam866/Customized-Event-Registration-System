package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    
    Optional<Registration> findByRegistrationId(String registrationId);
    
    List<Registration> findByEvent(Event event);
    
    List<Registration> findByEventAndCheckInStatus(Event event, Boolean checkInStatus);
    
    // FIXED: Use native query for JSON extraction
    @Query(value = "SELECT * FROM registrations r WHERE r.event_id = :eventId AND " +
           "(JSON_UNQUOTE(JSON_EXTRACT(r.form_data, '$.name')) LIKE CONCAT('%', :searchTerm, '%') OR " +
           "JSON_UNQUOTE(JSON_EXTRACT(r.form_data, '$.email')) LIKE CONCAT('%', :searchTerm, '%') OR " +
           "JSON_UNQUOTE(JSON_EXTRACT(r.form_data, '$.mobile')) LIKE CONCAT('%', :searchTerm, '%') OR " +
           "JSON_UNQUOTE(JSON_EXTRACT(r.form_data, '$.company')) LIKE CONCAT('%', :searchTerm, '%'))",
           nativeQuery = true)
    List<Registration> searchRegistrations(@Param("eventId") Long eventId, 
                                           @Param("searchTerm") String searchTerm);
    
    // Alternative: Search by name only (simpler)
    @Query(value = "SELECT * FROM registrations WHERE event_id = :eventId AND " +
           "JSON_UNQUOTE(JSON_EXTRACT(form_data, '$.name')) = :name",
           nativeQuery = true)
    List<Registration> findByName(@Param("eventId") Long eventId, 
                                  @Param("name") String name);
    
    // Search by email
    @Query(value = "SELECT * FROM registrations WHERE event_id = :eventId AND " +
           "JSON_UNQUOTE(JSON_EXTRACT(form_data, '$.email')) = :email",
           nativeQuery = true)
    List<Registration> findByEmail(@Param("eventId") Long eventId, 
                                   @Param("email") String email);
    
    // Search by mobile
    @Query(value = "SELECT * FROM registrations WHERE event_id = :eventId AND " +
           "JSON_UNQUOTE(JSON_EXTRACT(form_data, '$.mobile')) = :mobile",
           nativeQuery = true)
    List<Registration> findByMobile(@Param("eventId") Long eventId, 
                                    @Param("mobile") String mobile);
}