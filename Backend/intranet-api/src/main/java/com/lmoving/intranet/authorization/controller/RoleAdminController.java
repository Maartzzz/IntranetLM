package com.lmoving.intranet.authorization.controller;

import com.lmoving.intranet.authorization.dto.RoleDtos.*;
import com.lmoving.intranet.authorization.service.RoleAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleAdminController {
    private final RoleAdminService service;
    public RoleAdminController(RoleAdminService service) { this.service = service; }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public java.util.Map<String, String> badRequest(IllegalArgumentException ex) {
        return java.util.Map.of("message", ex.getMessage());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES.VIEW')")
    public List<RoleItem> list() { return service.listRoles(); }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLES.VIEW')")
    public List<PermissionItem> permissions() { return service.listPermissions(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLES.CREATE')")
    public RoleItem create(@Valid @RequestBody CreateRoleRequest request, Authentication auth) {
        return service.create(request, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES.EDIT')")
    public RoleItem update(@PathVariable long id, @Valid @RequestBody UpdateRoleRequest request, Authentication auth) {
        return service.update(id, request, auth.getName());
    }

    @PatchMapping("/{id}/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLES.DELETE')")
    public void active(@PathVariable long id, @RequestParam boolean active, Authentication auth) {
        service.setActive(id, active, auth.getName());
    }
}
