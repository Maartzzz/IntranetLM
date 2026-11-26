package com.lmoving.intranet.authorization.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Central read-only permission resolver. Enforcement is enabled in a later migration stage. */
@Service
public class PermissionService {
    private final JdbcTemplate jdbcTemplate;

    public PermissionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permissionCode) {
        Boolean protectedAdmin = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.username = ? AND r.code = 'ADMIN'
                  AND r.active = TRUE AND r.protected_role = TRUE
            )
            """, Boolean.class, username);
        if (Boolean.TRUE.equals(protectedAdmin)) return true;

        Boolean granted = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id AND r.active = TRUE
                JOIN role_permissions rp ON rp.role_id = r.id
                JOIN permissions p ON p.id = rp.permission_id AND p.active = TRUE
                WHERE ur.username = ? AND p.code = ?
            )
            """, Boolean.class, username, permissionCode);
        return Boolean.TRUE.equals(granted);
    }

    @Transactional(readOnly = true)
    public List<String> getUserPermissions(String username) {
        Boolean protectedAdmin = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.username = ? AND r.code = 'ADMIN'
                  AND r.active = TRUE AND r.protected_role = TRUE
            )
            """, Boolean.class, username);

        // Keep /api/auth/me aligned with the effective authorities assigned at authentication.
        // The protected administrator receives every active permission, including future ones.
        if (Boolean.TRUE.equals(protectedAdmin)) {
            return jdbcTemplate.queryForList("""
                SELECT code
                FROM permissions
                WHERE active = TRUE
                ORDER BY code
                """, String.class);
        }

        return jdbcTemplate.queryForList("""
            SELECT DISTINCT p.code
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id AND r.active = TRUE
            JOIN role_permissions rp ON rp.role_id = r.id
            JOIN permissions p ON p.id = rp.permission_id AND p.active = TRUE
            WHERE ur.username = ?
            ORDER BY p.code
            """, String.class, username);
    }
}
