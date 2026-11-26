package com.lmoving.intranet.security;

import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Loads current role and permission authorities from the database on every
 * authentication resolution. JWTs identify the user; they do not carry the
 * authorization snapshot, so role/permission changes take effect on the next request.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public CustomUserDetailsService(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Set<String> authorities = new LinkedHashSet<>();

        // Legacy role authority remains temporarily available while controllers migrate.
        if (user.getRole() != null) {
            authorities.add("ROLE_" + user.getRole().name());
        }

        authorities.addAll(jdbcTemplate.queryForList("""
                SELECT DISTINCT 'ROLE_' || r.code
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.username = ? AND r.active = TRUE
                """, String.class, username));

        Boolean protectedAdministrator = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM user_roles ur
                    JOIN roles r ON r.id = ur.role_id
                    WHERE ur.username = ? AND r.active = TRUE AND r.protected_role = TRUE
                )
                """, Boolean.class, username);

        if (Boolean.TRUE.equals(protectedAdministrator)) {
            // Protected administrator automatically receives current and future active permissions.
            authorities.addAll(jdbcTemplate.queryForList(
                    "SELECT code FROM permissions WHERE active = TRUE", String.class));
        } else {
            authorities.addAll(jdbcTemplate.queryForList("""
                    SELECT DISTINCT p.code
                    FROM user_roles ur
                    JOIN roles r ON r.id = ur.role_id AND r.active = TRUE
                    JOIN role_permissions rp ON rp.role_id = r.id
                    JOIN permissions p ON p.id = rp.permission_id AND p.active = TRUE
                    WHERE ur.username = ?
                    """, String.class, username));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities.stream().map(SimpleGrantedAuthority::new).toList())
                .disabled(!user.isActive())
                .build();
    }
}
