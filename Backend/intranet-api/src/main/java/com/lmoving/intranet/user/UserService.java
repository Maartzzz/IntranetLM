package com.lmoving.intranet.user;

import com.lmoving.intranet.authorization.entity.RoleEntity;
import com.lmoving.intranet.authorization.repository.RoleRepository;
import com.lmoving.intranet.user.dto.CreateUserRequest;
import com.lmoving.intranet.user.dto.UpdateUserRequest;
import com.lmoving.intranet.user.dto.UserResponse;
import com.lmoving.intranet.user.dto.UserRoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lmoving.intranet.audit.AuditLogService;
import java.util.*;

@Service
public class UserService {
    @Autowired private AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbcTemplate;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String username) {
        return toResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) throw new RuntimeException("El username ya está registrado");
        if (request.email() != null && userRepository.existsByEmail(request.email())) throw new RuntimeException("El email ya está registrado");
        if (userRepository.existsByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber())) throw new RuntimeException("El documento ya está registrado");

        List<RoleEntity> roles = resolveRoles(request.roles(), request.role());
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDocumentType(request.documentType());
        user.setDocumentNumber(request.documentNumber());
        user.setPhone(request.phone());
        user.setRole(legacyRole(roles, request.role()));
        user.setActive(true);
        User saved = userRepository.save(user);
        syncRoles(saved.getUsername(), roles);
        UserResponse response = toResponse(saved);
        auditLogService.registrar("USUARIOS", "USUARIO", null, saved.getUsername(), "CREATED",
                "Se creó el usuario " + saved.getUsername(), null, response.toString());
        return response;
    }

    @Transactional
    public UserResponse updateUser(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String oldSnapshot = toResponse(user).toString();
        if (request.email() != null && !request.email().equals(user.getEmail()) && userRepository.existsByEmail(request.email()))
            throw new RuntimeException("El email ya está registrado");
        boolean documentChanged = request.documentType() != user.getDocumentType() || !request.documentNumber().equals(user.getDocumentNumber());
        if (documentChanged && userRepository.existsByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber()))
            throw new RuntimeException("El documento ya está registrado");

        List<RoleEntity> roles = resolveRoles(request.roles(), request.role());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDocumentType(request.documentType());
        user.setDocumentNumber(request.documentNumber());
        user.setPhone(request.phone());
        user.setRole(legacyRole(roles, user.getRole()));
        User updated = userRepository.save(user);
        syncRoles(username, roles);
        UserResponse response = toResponse(updated);
        auditLogService.registrar("USUARIOS", "USUARIO", null, username, "UPDATED",
                "Se actualizaron los datos y roles del usuario " + username, oldSnapshot, response.toString());
        return response;
    }

    @Transactional
    public void setActive(String username, boolean active) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        boolean previous = user.isActive();
        user.setActive(active);
        userRepository.save(user);
        auditLogService.registrar("USUARIOS", "USUARIO", null, username, active ? "ACTIVATED" : "DEACTIVATED",
                active ? "Se activó el usuario " + username : "Se desactivó el usuario " + username,
                Boolean.toString(previous), Boolean.toString(active));
    }

    private List<RoleEntity> resolveRoles(List<String> requestedCodes, Role legacyRole) {
        List<String> codes = requestedCodes == null || requestedCodes.isEmpty()
                ? List.of(legacyRole == null ? Role.OPERACIONES.name() : legacyRole.name())
                : requestedCodes.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        if (codes.isEmpty()) throw new RuntimeException("Debe asignar al menos un rol");
        List<RoleEntity> result = new ArrayList<>();
        for (String code : codes) {
            RoleEntity role = roleRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Rol no encontrado: " + code));
            if (!role.isActive()) throw new RuntimeException("No se puede asignar un rol inactivo: " + code);
            result.add(role);
        }
        return result;
    }

    private void syncRoles(String username, List<RoleEntity> roles) {
        jdbcTemplate.update("DELETE FROM user_roles WHERE username = ?", username);
        for (RoleEntity role : roles) jdbcTemplate.update(
                "INSERT INTO user_roles (username, role_id) VALUES (?, ?) ON CONFLICT (username, role_id) DO NOTHING",
                username, role.getId());
    }

    private Role legacyRole(List<RoleEntity> roles, Role fallback) {
        for (RoleEntity role : roles) {
            try { return Role.valueOf(role.getCode()); } catch (IllegalArgumentException ignored) { }
        }
        return fallback == null ? Role.OPERACIONES : fallback;
    }

    private UserResponse toResponse(User user) {
        List<UserRoleResponse> roles = jdbcTemplate.query(
                "SELECT r.code, r.name FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE ur.username = ? ORDER BY r.name",
                (rs, rowNum) -> new UserRoleResponse(rs.getString("code"), rs.getString("name")), user.getUsername());
        if (roles.isEmpty() && user.getRole() != null) {
            roles = roleRepository.findByCode(user.getRole().name())
                    .map(r -> List.of(new UserRoleResponse(r.getCode(), r.getName()))).orElse(List.of());
        }
        return new UserResponse(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getDocumentType(), user.getDocumentNumber(), user.getPhone(), user.getRole(), user.isActive(), roles);
    }
}
