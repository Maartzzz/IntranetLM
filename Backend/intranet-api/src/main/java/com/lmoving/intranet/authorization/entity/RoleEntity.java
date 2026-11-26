package com.lmoving.intranet.authorization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 300)
    private String description;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "system_role", nullable = false)
    private boolean systemRole;
    @Column(name = "protected_role", nullable = false)
    private boolean protectedRole;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public boolean isSystemRole() { return systemRole; }
    public boolean isProtectedRole() { return protectedRole; }
}
