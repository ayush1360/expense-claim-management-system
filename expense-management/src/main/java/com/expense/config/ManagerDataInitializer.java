package com.expense.config;

import com.expense.entity.User;
import com.expense.enums.Role;
import com.expense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ManagerDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.manager.email:manager@test.com}")
    private String managerEmail;

    @Value("${app.seed.manager.password:Manager@123}")
    private String managerPassword;

    @Bean
    public CommandLineRunner seedManager() {
        return args -> {
            if (userRepository.existsByEmail(managerEmail)) {
                System.out.println("Manager account already exists.");
                return;
            }

            User manager = User.builder()
                    .fullName("System Manager")
                    .email(managerEmail)
                    .password(passwordEncoder.encode(managerPassword))
                    .role(Role.MANAGER)
                    .active(true)
                    .build();

            userRepository.save(manager);

            System.out.println("Default Manager account created.");
        };
    }
}