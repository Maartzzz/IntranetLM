package com.lmoving.intranet.audit;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="actor_username", nullable=false, length=50) private String actorUsername;
    @Column(name="actor_name", nullable=false, length=201) private String actorName;
    @Column(nullable=false, length=40) private String module;
    @Column(name="entity_type", nullable=false, length=40) private String entityType;
    @Column(name="entity_id") private Long entityId;
    @Column(name="entity_key", length=100) private String entityKey;
    @Column(nullable=false, length=40) private String action;
    @Column(nullable=false, length=500) private String description;
    @Column(name="old_value", columnDefinition="TEXT") private String oldValue;
    @Column(name="new_value", columnDefinition="TEXT") private String newValue;
    @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;
    @Column(name="legacy_activity_id", unique=true) private Long legacyActivityId;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId(){return id;}
    public String getActorUsername(){return actorUsername;} public void setActorUsername(String v){actorUsername=v;}
    public String getActorName(){return actorName;} public void setActorName(String v){actorName=v;}
    public String getModule(){return module;} public void setModule(String v){module=v;}
    public String getEntityType(){return entityType;} public void setEntityType(String v){entityType=v;}
    public Long getEntityId(){return entityId;} public void setEntityId(Long v){entityId=v;}
    public String getEntityKey(){return entityKey;} public void setEntityKey(String v){entityKey=v;}
    public String getAction(){return action;} public void setAction(String v){action=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getOldValue(){return oldValue;} public void setOldValue(String v){oldValue=v;}
    public String getNewValue(){return newValue;} public void setNewValue(String v){newValue=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public Long getLegacyActivityId(){return legacyActivityId;} public void setLegacyActivityId(Long v){legacyActivityId=v;}
}
