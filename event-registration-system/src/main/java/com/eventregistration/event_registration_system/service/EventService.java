package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.Event;
import com.eventregistration.event_registration_system.entity.User;
import com.eventregistration.event_registration_system.enums.EventStatus;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import com.eventregistration.event_registration_system.exception.ResourceNotFoundException;
import com.eventregistration.event_registration_system.repository.EventRepository;
import com.eventregistration.event_registration_system.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final AuthService authService;
    private final JsonConverter jsonConverter;

    /**
     * Create a new event
     */
    @Transactional
    public Event createEvent(Event event, Long createdByUserId) {
        User createdBy = authService.getUserById(createdByUserId);
        event.setCreatedBy(createdBy);
        event.setStatus(EventStatus.DRAFT);
        
        // Set default badge fields if not provided
        if (event.getBadgeFields() == null) {
            event.setBadgeFields("[\"Name\", \"Company\"]");
        }
        
        log.info("Creating new event: {}", event.getName());
        Event savedEvent = eventRepository.save(event);
        savedEvent.getCreatedBy().getId();
        
        return savedEvent;
    }

    /**
     * Get event by ID
     */
    public Event getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        event.getCreatedBy().getId();
        return event;
    }

    /**
     * Get all events
     */
    public List<Event> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        events.forEach(event -> event.getCreatedBy().getId());
        return events;
    }

    /**
     * Get events by status
     */
    public List<Event> getEventsByStatus(EventStatus status) {
        List<Event> events = eventRepository.findByStatus(status);
        events.forEach(event -> event.getCreatedBy().getId());
        return events;
    }

    /**
     * Get active events
     */
    public List<Event> getActiveEvents() {
        List<Event> events = eventRepository.findByStatusAndEventDateAfter(EventStatus.ACTIVE, LocalDate.now());
        events.forEach(event -> event.getCreatedBy().getId());
        return events;
    }

    /**
     * Update event
     */
    @Transactional
    public Event updateEvent(Long eventId, Event updatedEvent) {
        Event existingEvent = getEventById(eventId);
        
        existingEvent.setName(updatedEvent.getName());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setMaxAttendees(updatedEvent.getMaxAttendees());
        existingEvent.setEventLogoUrl(updatedEvent.getEventLogoUrl());
        
        // Update badge fields if provided
        if (updatedEvent.getBadgeFields() != null) {
            existingEvent.setBadgeFields(updatedEvent.getBadgeFields());
        }
        
        log.info("Updating event: {}", existingEvent.getName());
        Event savedEvent = eventRepository.save(existingEvent);
        savedEvent.getCreatedBy().getId();
        
        return savedEvent;
    }

    /**
     * Update event status
     */
    @Transactional
    public Event updateEventStatus(Long eventId, EventStatus status) {
        Event event = getEventById(eventId);
        event.setStatus(status);
        log.info("Event {} status updated to: {}", event.getName(), status);
        Event savedEvent = eventRepository.save(event);
        savedEvent.getCreatedBy().getId();
        return savedEvent;
    }

    /**
     * Update badge fields for an event
     */
    @Transactional
    public Event updateBadgeFields(Long eventId, List<String> badgeFields) {
        Event event = getEventById(eventId);
        event.setBadgeFields(jsonConverter.toJson(badgeFields));
        log.info("Badge fields updated for event {}: {}", event.getName(), badgeFields);
        Event savedEvent = eventRepository.save(event);
        savedEvent.getCreatedBy().getId();
        return savedEvent;
    }

    /**
     * Get badge fields for an event
     */
    public List<String> getBadgeFields(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getBadgeFields() == null) {
            return List.of("Name", "Company"); // Default
        }
        return jsonConverter.fromJson(event.getBadgeFields(), List.class);
    }

    /**
     * Delete event
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
        log.info("Event deleted: {}", event.getName());
    }

    /**
     * Check if event is active
     */
    public boolean isEventActive(Long eventId) {
        Event event = getEventById(eventId);
        return event.getStatus() == EventStatus.ACTIVE;
    }
}