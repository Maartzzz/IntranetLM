package com.lmoving.intranet.config;

import com.lmoving.intranet.user.Role;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.DocumentType;
import com.lmoving.intranet.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.password:}") String initialAdminPassword
    ) {
        return args -> {

            if (userRepository.existsByUsername("admin")) {
                return;
            }

            if (initialAdminPassword == null || initialAdminPassword.length() < 12) {
                throw new IllegalStateException(
                        "Debe configurar APP_BOOTSTRAP_ADMIN_PASSWORD con al menos 12 caracteres " +
                        "para crear el usuario administrador inicial."
                );
            }

            User admin = new User();

            admin.setUsername("admin");
            admin.setEmail("admin@empresa.com");

            admin.setPasswordHash(
                    passwordEncoder.encode(initialAdminPassword)
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