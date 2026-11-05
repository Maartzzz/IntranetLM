package com.lmoving.intranet.osinterna.schedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;

@Entity
@Table(name = "internal_service_order_schedule_versions",
 uniqueConstraints = @UniqueConstraint(name = "uq_ios_schedule_version_period",
 columnNames = {"start_date", "end_date", "version_number"}))
public class InternalServiceOrderScheduleVersion {
    
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name="version_number", nullable=false) private Integer versionNumber;
 @Column(name="start_date", nullable=false) private LocalDate startDate;
 @Column(name="end_date", nullable=false) private LocalDate endDate;
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "snapshot", nullable = false, columnDefinition = "jsonb")
private List<Map<String, Object>> snapshot;
 @Column(name="created_by_username", nullable=false, length=50) private String createdByUsername;
 @Column(name="created_by_name", nullable=false, length=210) private String createdByName;
 @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;

protected InternalServiceOrderScheduleVersion() {}

public InternalServiceOrderScheduleVersion(
    Integer versionNumber,
    LocalDate startDate,
    LocalDate endDate,
    List<Map<String, Object>> snapshot,
    String createdByUsername,
    String createdByName
) {
    this.versionNumber = versionNumber;
    this.startDate = startDate;
    this.endDate = endDate;
    this.snapshot = snapshot;
    this.createdByUsername = createdByUsername;
    this.createdByName = createdByName;
}

@PrePersist void onCreate(){ createdAt=LocalDateTime.now(); }
public Long getId(){return id;} public Integer getVersionNumber(){return versionNumber;}
public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;}

public List<Map<String, Object>> getSnapshot() {
    return snapshot;
}

public String getCreatedByUsername(){return createdByUsername;} public String getCreatedByName(){return createdByName;}
public LocalDateTime getCreatedAt(){return createdAt;}
}
