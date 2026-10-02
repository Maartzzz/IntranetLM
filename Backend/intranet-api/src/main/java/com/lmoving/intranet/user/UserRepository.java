package com.lmoving.intranet.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByDocumentTypeAndDocumentNumber(
        DocumentType documentType,
        String documentNumber
    );
    List<User> findByRole(Role role);
}
