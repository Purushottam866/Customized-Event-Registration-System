package com.eventregistration.event_registration_system.service;

import com.eventregistration.event_registration_system.entity.User;
import com.eventregistration.event_registration_system.enums.Role;
import com.eventregistration.event_registration_system.exception.BadRequestException;
import com.eventregistration.event_registration_system.exception.ResourceNotFoundException;
import com.eventregistration.event_registration_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user (Admin only)
     */
    @Transactional
    public User registerUser(String username, String password, String email, String fullName, Role role) {
        // Check if email exists (PRIMARY UNIQUE CONSTRAINT)
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered: " + email);
        }

        // Username can be same - no check needed

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role != null ? role : Role.USER);
        user.setIsActive(true);

        log.info("Registering new user: {} with email: {}", username, email);
        return userRepository.save(user);
    }

    /**
     * Get user by username (ADDED THIS METHOD)
     */
    public User getUserByUsername(String username) {
        // Since username is not unique, this will return the first match
        // Better to use email for unique identification
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Update user status (activate/deactivate)
     */
    @Transactional
    public User updateUserStatus(Long userId, Boolean isActive) {
        User user = getUserById(userId);
        user.setIsActive(isActive);
        log.info("User {} status updated to: {}", user.getEmail(), isActive);
        return userRepository.save(user);
    }

    /**
     * Update user email
     */
    @Transactional
    public User updateUserEmail(Long userId, String newEmail) {
        User user = getUserById(userId);
        
        // Check if email is already taken by another user
        User existingUser = userRepository.findByEmail(newEmail).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BadRequestException("Email already registered: " + newEmail);
        }
        
        user.setEmail(newEmail);
        log.info("User {} email updated to: {}", user.getUsername(), newEmail);
        return userRepository.save(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public User updateUserProfile(Long userId, String fullName, String email) {
        User user = getUserById(userId);
        
        // Check if email is already taken by another user
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BadRequestException("Email already registered: " + email);
        }
        
        user.setFullName(fullName);
        user.setEmail(email);
        log.info("User {} profile updated", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }
}