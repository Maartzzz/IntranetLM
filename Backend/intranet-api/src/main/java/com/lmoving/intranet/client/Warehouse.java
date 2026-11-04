package com.lmoving.intranet.client;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "warehouses", uniqueConstraints = {
    @UniqueConstraint(name = "uq_warehouses_location", columnNames = "location_id"),
    @UniqueConstraint(name = "uq_warehouses_code", columnNames = "warehouse_code")
})
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "warehouse_code", nullable = false, length = 30)
    private String warehouseCode;
    @Column(name = "warehouse_name", nullable = false, length = 150)
    private String warehouseName;
    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 30)
    private WarehouseType warehouseType;
    @Column(name = "operator_name", nullable = false, length = 150)
    private String operatorName;
    @Column(nullable = false) private boolean active = true;
    @Column(columnDefinition = "TEXT") private String observations;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public Warehouse() {}
    @PrePersist protected void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Location getLocation() { return location; } public void setLocation(Location v) { location = v; }
    public String getWarehouseCode() { return warehouseCode; } public void setWarehouseCode(String v) { warehouseCode = v; }
    public String getWarehouseName() { return warehouseName; } public void setWarehouseName(String v) { warehouseName = v; }
    public WarehouseType getWarehouseType() { return warehouseType; } public void setWarehouseType(WarehouseType v) { warehouseType = v; }
    public String getOperatorName() { return operatorName; } public void setOperatorName(String v) { operatorName = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public String getObservations() { return observations; } public void setObservations(String v) { observations = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
