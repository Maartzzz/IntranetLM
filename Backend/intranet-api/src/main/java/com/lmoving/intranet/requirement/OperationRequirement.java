package com.lmoving.intranet.requirement;

import java.time.LocalDateTime;
import com.lmoving.intranet.operation.Operation;
import com.lmoving.intranet.oscliente.ClientServiceOrder;
import com.lmoving.intranet.osinterna.InternalServiceOrder;
import com.lmoving.intranet.status.Status;
import jakarta.persistence.*;

@Entity
@Table(name = "operation_requirements")
public class OperationRequirement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private Operation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_service_order_id")
    private ClientServiceOrder clientServiceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_service_order_id")
    private InternalServiceOrder internalServiceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_catalog_id")
    private RequirementCatalog catalog;

    @Column(name = "custom_name", length = 120)
    private String customName;

    @Column(name = "external_responsible", length = 150)
    private String externalResponsible;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OperationRequirement() {}
    @PrePersist void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        registeredAt = now; updatedAt = now;
    }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Operation getOperation() { return operation; }
    public void setOperation(Operation v) { operation = v; }
    public ClientServiceOrder getClientServiceOrder() { return clientServiceOrder; }
    public void setClientServiceOrder(ClientServiceOrder v) { clientServiceOrder = v; }
    public InternalServiceOrder getInternalServiceOrder() { return internalServiceOrder; }
    public void setInternalServiceOrder(InternalServiceOrder v) { internalServiceOrder = v; }
    public RequirementCatalog getCatalog() { return catalog; }
    public void setCatalog(RequirementCatalog v) { catalog = v; }
    public String getCustomName() { return customName; }
    public void setCustomName(String v) { customName = v; }
    public String getExternalResponsible() { return externalResponsible; }
    public void setExternalResponsible(String v) { externalResponsible = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public String getComment() { return comment; }
    public void setComment(String v) { comment = v; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
