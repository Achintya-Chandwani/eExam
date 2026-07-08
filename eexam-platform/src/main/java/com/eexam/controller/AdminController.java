package com.eexam.controller;

import com.eexam.dto.CreateUserRequest;
import com.eexam.dto.ExamSummaryResponse;
import com.eexam.dto.ResetPasswordRequest;
import com.eexam.model.Role;
import com.eexam.model.User;
import com.eexam.repository.ExamRepository;
import com.eexam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final ExamRepository examRepository;

    public AdminController(UserService userService, ExamRepository examRepository) {
        this.userService = userService;
        this.examRepository = examRepository;
    }

    // ---- User management ----

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/users")
    public List<User> listUsers(@RequestParam(required = false) String role) {
        if (role != null && !role.isBlank()) {
            return userService.getUsersByRole(Role.valueOf(role.toUpperCase()));
        }
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Map.of("message", "User deleted successfully");
    }

    @PostMapping("/users/{id}/reset-password")
    public Map<String, String> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return Map.of("message", "Password reset successfully");
    }

    @PostMapping("/users/{id}/enable")
    public Map<String, String> enableUser(@PathVariable Long id) {
        userService.setEnabled(id, true);
        return Map.of("message", "User enabled");
    }

    @PostMapping("/users/{id}/disable")
    public Map<String, String> disableUser(@PathVariable Long id) {
        userService.setEnabled(id, false);
        return Map.of("message", "User disabled");
    }

    // ---- Exam oversight ----

    @GetMapping("/exams")
    public List<ExamSummaryResponse> viewAllExams() {
        return examRepository.findAll()
                .stream()
                .map(ExamSummaryResponse::from)
                .toList();
    }
}
