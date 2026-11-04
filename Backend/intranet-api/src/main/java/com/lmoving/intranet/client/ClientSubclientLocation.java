package com.lmoving.intranet.client;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "client_subclient_locations", uniqueConstraints = @UniqueConstraint(name = "uq_csl_relation_location", columnNames = {"client_subclient_id", "location_id"}))
public class ClientSubclientLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_subclient_id", nullable = false) private ClientSubclient clientSubclient;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "location_id", nullable = false) private Location location;
    @Enumerated(EnumType.STRING) @Column(name = "usage_type", nullable = false, length = 20) private LocationUsageType usageType;
    @Column(name = "contact_name", length = 150) private String contactName;
    @Column(name = "contact_phone", length = 30) private String contactPhone;
    @Column(name = "operating_hours", length = 200) private String operatingHours;
    @Column(columnDefinition = "TEXT") private String instructions;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public ClientSubclientLocation() {}
    @PrePersist protected void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public ClientSubclient getClientSubclient() { return clientSubclient; } public void setClientSubclient(ClientSubclient v) { clientSubclient = v; }
    public Location getLocation() { return location; } public void setLocation(Location v) { location = v; }
    public LocationUsageType getUsageType() { return usageType; } public void setUsageType(LocationUsageType v) { usageType = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { contactName = v; }
    public String getContactPhone() { return contactPhone; } public void setContactPhone(String v) { contactPhone = v; }
    public String getOperatingHours() { return operatingHours; } public void setOperatingHours(String v) { operatingHours = v; }
    public String getInstructions() { return instructions; } public void setInstructions(String v) { instructions = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
