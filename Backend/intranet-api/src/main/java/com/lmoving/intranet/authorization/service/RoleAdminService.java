package com.lmoving.intranet.authorization.service;

import com.lmoving.intranet.authorization.dto.RoleDtos.*;
import com.lmoving.intranet.audit.AuditLogService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class RoleAdminService {
    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{1,49}");
    private final JdbcTemplate jdbc;
    private final AuditLogService auditLogService;

    public RoleAdminService(JdbcTemplate jdbc, AuditLogService auditLogService) {
        this.jdbc = jdbc;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<RoleItem> listRoles() {
        List<RoleItem> rows = jdbc.query("""
            SELECT r.id, r.code, r.name, r.description, r.active, r.system_role, r.protected_role,
                   (SELECT COUNT(*) FROM user_roles ur WHERE ur.role_id = r.id) AS user_count,
                   (SELECT COUNT(*) FROM role_permissions rp WHERE rp.role_id = r.id) AS permission_count
            FROM roles r ORDER BY r.protected_role DESC, r.name
            """, (rs, n) -> new RoleItem(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                rs.getString("description"), rs.getBoolean("active"), rs.getBoolean("system_role"),
                rs.getBoolean("protected_role"), rs.getInt("user_count"), rs.getInt("permission_count"), List.of()));
        List<RoleItem> result = new ArrayList<>();
        for (RoleItem role : rows) {
            result.add(new RoleItem(role.id(), role.code(), role.name(), role.description(), role.active(),
                role.systemRole(), role.protectedRole(), role.userCount(), role.permissionCount(), permissionIds(role.id())));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<PermissionItem> listPermissions() {
        return jdbc.query("""
            SELECT p.id, p.code, p.name, p.description, m.code module_code, m.name module_name,
                   m.display_order module_order
            FROM permissions p JOIN permission_modules m ON m.id = p.module_id
            WHERE p.active = TRUE AND m.active = TRUE
            ORDER BY m.display_order, p.name
            """, (rs, n) -> new PermissionItem(rs.getLong("id"), rs.getString("code"),
                rs.getString("name"), rs.getString("description"), rs.getString("module_code"),
                rs.getString("module_name"), rs.getInt("module_order")));
    }

    @Transactional
    public RoleItem create(CreateRoleRequest request, String actor) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (!CODE.matcher(code).matches())
            throw new IllegalArgumentException("El código debe usar mayúsculas, números o guion bajo y comenzar con una letra.");
        validatePermissions(request.permissionIds());
        try {
            Long id = jdbc.queryForObject("""
                INSERT INTO roles(code, name, description, active, system_role, protected_role, created_at, updated_at)
                VALUES (?, ?, ?, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id
                """, Long.class, code, request.name().trim(), clean(request.description()));
            replacePermissions(id, request.permissionIds());
            audit(actor, "CREATE", id, "role_created");
            RoleItem created = getRole(id);
            auditLogService.registrarComoUsuario(actor, "SEGURIDAD", "ROL", id, "CREATED",
                    "Se creó el rol " + created.code(), null, created.toString());
            return created;
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Ya existe un rol con ese código.");
        }
    }

    @Transactional
    public RoleItem update(long id, UpdateRoleRequest request, String actor) {
        RoleItem existing = getRole(id);
        validatePermissions(request.permissionIds());
        jdbc.update("UPDATE roles SET name = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            request.name().trim(), clean(request.description()), id);
        replacePermissions(id, request.permissionIds());
        audit(actor, "UPDATE", id, "role_updated");
        RoleItem updated = getRole(id);
        auditLogService.registrarComoUsuario(actor, "SEGURIDAD", "ROL", id, "UPDATED",
                "Se actualizaron los datos y permisos del rol " + updated.code(),
                existing.toString(), updated.toString());
        return updated;
    }

    @Transactional
    public void setActive(long id, boolean active, String actor) {
        RoleItem role = getRole(id);
        if (role.protectedRole())
            throw new IllegalArgumentException("El rol Administrador principal está protegido y no puede desactivarse.");
        jdbc.update("UPDATE roles SET active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", active, id);
        audit(actor, active ? "ACTIVATE" : "DEACTIVATE", id, active ? "role_activated" : "role_deactivated");
        RoleItem updated = getRole(id);
        auditLogService.registrarComoUsuario(actor, "SEGURIDAD", "ROL", id,
                active ? "ACTIVATED" : "DEACTIVATED",
                (active ? "Se activó el rol " : "Se desactivó el rol ") + updated.code(),
                role.toString(), updated.toString());
    }

    private RoleItem getRole(long id) {
        return jdbc.query("""
            SELECT r.id, r.code, r.name, r.description, r.active, r.system_role, r.protected_role,
                   (SELECT COUNT(*) FROM user_roles ur WHERE ur.role_id = r.id) user_count,
                   (SELECT COUNT(*) FROM role_permissions rp WHERE rp.role_id = r.id) permission_count
            FROM roles r WHERE r.id = ?
            """, rs -> {
                if (!rs.next()) throw new IllegalArgumentException("No se encontró el rol solicitado.");
                return new RoleItem(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                    rs.getString("description"), rs.getBoolean("active"), rs.getBoolean("system_role"),
                    rs.getBoolean("protected_role"), rs.getInt("user_count"), rs.getInt("permission_count"),
                    permissionIds(id));
            }, id);
    }

    private List<Long> permissionIds(long roleId) {
        return jdbc.queryForList("SELECT permission_id FROM role_permissions WHERE role_id = ? ORDER BY permission_id",
            Long.class, roleId);
    }

    private void validatePermissions(List<Long> ids) {
        if (ids == null) throw new IllegalArgumentException("Debe enviar la lista de permisos.");
        if (new HashSet<>(ids).size() != ids.size()) throw new IllegalArgumentException("La lista contiene permisos repetidos.");
        if (ids.isEmpty()) return;
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM permissions WHERE active = TRUE AND id IN (" +
            String.join(",", Collections.nCopies(ids.size(), "?")) + ")", Integer.class, ids.toArray());
        if (count == null || count != ids.size()) throw new IllegalArgumentException("Uno o más permisos no existen o están inactivos.");
    }

    private void replacePermissions(long roleId, List<Long> ids) {
        jdbc.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        for (Long permissionId : ids)
            jdbc.update("INSERT INTO role_permissions(role_id, permission_id) VALUES (?, ?)", roleId, permissionId);
    }

    private void audit(String actor, String action, long id, String details) {
        jdbc.update("INSERT INTO authorization_audit(actor_username, action, target_type, target_id, details) " +
            "VALUES (?, ?, 'ROLE', ?, jsonb_build_object('event', ?))", actor, action, Long.toString(id), details);
    }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
