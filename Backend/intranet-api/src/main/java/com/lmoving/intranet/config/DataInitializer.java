package com.lmoving.intranet.config;

import com.lmoving.intranet.user.Role;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.DocumentType;
import com.lmoving.intranet.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (userRepository.existsByUsername("admin")) {
                return;
            }

            User admin = new User();

            admin.setUsername("admin");
            admin.setEmail("admin@empresa.com");

            admin.setPasswordHash(
                    passwordEncoder.encode("Admin123!")
            );

            admin.setFirstName("Administrador");
            admin.setLastName("Sistema");

            admin.setDocumentType(DocumentType.OTRO);
            admin.setDocumentNumber("SYSTEM-001");

            admin.setPhone(null);

            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
        };
    }
}