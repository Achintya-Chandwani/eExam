package com.eexam.service;

import com.eexam.dto.CreateUserRequest;
import com.eexam.model.Role;
import com.eexam.model.User;
import com.eexam.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        User user = new User(
                request.getUsername(),
                request.getPassword(),   // stored as plain text
                request.getFullName(),
                request.getEmail(),
                role
        );
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User updateUser(Long id, CreateUserRequest request) {
        User user = getById(id);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());   // stored as plain text
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void resetPassword(Long id, String newPassword) {
        User user = getById(id);
        user.setPassword(newPassword);   // stored as plain text
        userRepository.save(user);
    }

    public void setEnabled(Long id, boolean enabled) {
        User user = getById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
