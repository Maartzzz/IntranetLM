package com.lmoving.intranet.activity;

import java.time.LocalDateTime;
import com.lmoving.intranet.operation.Operation;
import jakarta.persistence.*;

@Entity
@Table(name = "operation_activity")
public class OperationActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;
    @Column(name = "actor_username", nullable = false, length = 50)
    private String actorUsername;
    @Column(name = "actor_name", nullable = false, length = 201)
    private String actorName;
    @Column(nullable = false, length = 40)
    private String action;
    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType;
    @Column(name = "entity_id")
    private Long entityId;
    @Column(nullable = false, length = 500)
    private String description;
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Operation getOperation() { return operation; }
    public void setOperation(Operation v) { operation = v; }
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String v) { actorUsername = v; }
    public String getActorName() { return actorName; }
    public void setActorName(String v) { actorName = v; }
    public String getAction() { return action; }
    public void setAction(String v) { action = v; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String v) { entityType = v; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long v) { entityId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String v) { oldValue = v; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String v) { newValue = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
