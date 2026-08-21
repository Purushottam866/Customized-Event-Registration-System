package com.eventregistration.event_registration_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventRegistrationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventRegistrationSystemApplication.class, args);
	}

}
 