package com.eventregistration.event_registration_system.repository;

import com.eventregistration.event_registration_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);  // ADD THIS METHOD
    boolean existsByEmail(String email);
}