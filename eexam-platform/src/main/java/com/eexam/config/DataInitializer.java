package com.eexam.config;

import com.eexam.model.Role;
import com.eexam.model.User;
import com.eexam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-username}")
    private String defaultAdminUsername;

    @Value("${app.admin.default-password}")
    private String defaultAdminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(defaultAdminUsername)) {
            return;
        }
        User admin = new User(
                defaultAdminUsername,
                passwordEncoder.encode(defaultAdminPassword),
                "System Administrator",
                "admin@eexam.local",
                Role.ADMIN
        );
        userRepository.save(admin);
        System.out.println("=====================================================");
        System.out.println(" Default admin created -> username: " + defaultAdminUsername + " | password: " + defaultAdminPassword);
        System.out.println(" Please log in and change this password immediately.");
        System.out.println("=====================================================");
    }
}
