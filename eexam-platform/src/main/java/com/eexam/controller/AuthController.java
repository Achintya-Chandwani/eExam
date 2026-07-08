package com.eexam.controller;

import com.eexam.dto.LoginRequest;
import com.eexam.dto.LoginResponse;
import com.eexam.model.User;
import com.eexam.repository.UserRepository;
import com.eexam.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final com.eexam.security.CustomUserDetailsService userDetailsService;

    public AuthController(JwtUtil jwtUtil,
                          UserRepository userRepository,
                          com.eexam.security.CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // Direct plain-text password comparison (no BCrypt)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole().name());
    }
}
