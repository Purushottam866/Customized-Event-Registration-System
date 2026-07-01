package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);
    List<Event> findByEventDate(LocalDate eventDate);
    List<Event> findByStatusAndEventDateAfter(EventStatus status, LocalDate date);
}