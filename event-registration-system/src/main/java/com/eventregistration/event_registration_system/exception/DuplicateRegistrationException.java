package com.eventregistration.event_registration_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRegistrationException extends RuntimeException {
    
    public DuplicateRegistrationException(String message) {
        super(message);
    }
}