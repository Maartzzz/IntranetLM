package com.lmoving.intranet.authorization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class RoleDtos {
    private RoleDtos() {}

    public record PermissionItem(Long id, String code, String name, String description,
                                 String moduleCode, String moduleName, int moduleOrder) {}
    public record RoleItem(Long id, String code, String name, String description,
                           boolean active, boolean systemRole, boolean protectedRole,
                           int userCount, int permissionCount, List<Long> permissionIds) {}
    public record CreateRoleRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 120) String name,
            @Size(max = 300) String description,
            @NotNull List<Long> permissionIds) {}
    public record UpdateRoleRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 300) String description,
            @NotNull List<Long> permissionIds) {}
}
