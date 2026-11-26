package com.lmoving.intranet.authorization.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class PermissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "module_id", nullable = false)
    private Long moduleId;
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 300)
    private String description;
    @Column(nullable = false)
    private boolean active;
    public Long getId() { return id; }
    public Long getModuleId() { return moduleId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}
