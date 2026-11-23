package com.lmoving.intranet.user;

import com.lmoving.intranet.user.dto.CreateUserRequest;
import com.lmoving.intranet.user.dto.UpdateUserRequest;
import com.lmoving.intranet.user.dto.UserResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.lmoving.intranet.audit.AuditLogService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired private AuditLogService auditLogService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException(
                "El username ya está registrado"
            );
        }

        if (request.email() != null &&
                userRepository.existsByEmail(request.email())) {

            throw new RuntimeException(
                "El email ya está registrado"
            );
        }

        if (userRepository.existsByDocumentTypeAndDocumentNumber(
                request.documentType(),
                request.documentNumber()
        )) {

            throw new RuntimeException(
                "El documento ya está registrado"
            );
        }

        User user = new User();

        user.setUsername(request.username());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDocumentType(request.documentType());
        user.setDocumentNumber(request.documentNumber());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        UserResponse response = toResponse(savedUser);
        auditLogService.registrar("USUARIOS", "USUARIO", null, savedUser.getUsername(), "CREATED",
            "Se creó el usuario " + savedUser.getUsername(), null, response.toString());
        return response;
    }

    @Transactional
    public UserResponse updateUser(
        String username,
        UpdateUserRequest request
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );
        String oldSnapshot = toResponse(user).toString();

        if (request.email() != null &&
                !request.email().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.email())) {

            throw new RuntimeException(
                "El email ya está registrado"
            );
        }

        boolean documentChanged =
                request.documentType() != user.getDocumentType()
                || !request.documentNumber()
                        .equals(user.getDocumentNumber());

        if (documentChanged &&
                userRepository.existsByDocumentTypeAndDocumentNumber(
                        request.documentType(),
                        request.documentNumber()
                )) {

            throw new RuntimeException(
                "El documento ya está registrado"
            );
        }
        
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDocumentType(request.documentType());
        user.setDocumentNumber(request.documentNumber());
        user.setPhone(request.phone());
        user.setRole(request.role());

        User updatedUser = userRepository.save(user);
        UserResponse response = toResponse(updatedUser);
        auditLogService.registrar("USUARIOS", "USUARIO", null, updatedUser.getUsername(), "UPDATED",
            "Se actualizaron los datos del usuario " + updatedUser.getUsername(),
            oldSnapshot, response.toString());
        return response;
    }

    @Transactional
    public void setActive(
        String username,
        boolean active
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        boolean previous = user.isActive();
        user.setActive(active);
        userRepository.save(user);
        auditLogService.registrar("USUARIOS", "USUARIO", null, username, active ? "ACTIVATED" : "DEACTIVATED",
            active ? "Se activó el usuario " + username : "Se desactivó el usuario " + username,
            Boolean.toString(previous), Boolean.toString(active));
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDocumentType(),
                user.getDocumentNumber(),
                user.getPhone(),
                user.getRole(),
                user.isActive()
        );
    }
}