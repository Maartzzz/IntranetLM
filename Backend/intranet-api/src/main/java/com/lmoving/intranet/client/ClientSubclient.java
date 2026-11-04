package com.lmoving.intranet.client;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "client_subclients", uniqueConstraints = @UniqueConstraint(name = "uq_client_subclients_pair", columnNames = {"client_id", "subclient_id"}))
public class ClientSubclient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false) private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subclient_id", nullable = false) private Client subclient;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public ClientSubclient() {}
    @PrePersist protected void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public Client getClient() { return client; } public void setClient(Client v) { client = v; }
    public Client getSubclient() { return subclient; } public void setSubclient(Client v) { subclient = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
