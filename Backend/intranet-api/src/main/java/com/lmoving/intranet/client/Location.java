package com.lmoving.intranet.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String name;
    @Enumerated(EnumType.STRING) @Column(name = "location_type", nullable = false, length = 30) private LocationType locationType;
    @Column(nullable = false, length = 300) private String address;
    @Column(length = 300) private String reference;
    @Column(length = 100) private String district;
    @Column(length = 100) private String province;
    @Column(length = 100) private String department;
    @Column(precision = 10, scale = 7) private BigDecimal latitude;
    @Column(precision = 10, scale = 7) private BigDecimal longitude;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public Location() {}
    @PrePersist protected void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public String getName() { return name; } public void setName(String v) { name = v; }
    public LocationType getLocationType() { return locationType; } public void setLocationType(LocationType v) { locationType = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getReference() { return reference; } public void setReference(String v) { reference = v; }
    public String getDistrict() { return district; } public void setDistrict(String v) { district = v; }
    public String getProvince() { return province; } public void setProvince(String v) { province = v; }
    public String getDepartment() { return department; } public void setDepartment(String v) { department = v; }
    public BigDecimal getLatitude() { return latitude; } public void setLatitude(BigDecimal v) { latitude = v; }
    public BigDecimal getLongitude() { return longitude; } public void setLongitude(BigDecimal v) { longitude = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
