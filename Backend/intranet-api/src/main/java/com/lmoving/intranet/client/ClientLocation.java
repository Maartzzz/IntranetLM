package com.lmoving.intranet.client;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "client_locations", uniqueConstraints = {
    @UniqueConstraint(name = "uq_client_locations_pair", columnNames = {"client_id", "location_id"}),
    @UniqueConstraint(name = "uq_client_locations_location", columnNames = {"location_id"})
})
public class ClientLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false) private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "location_id", nullable = false) private Location location;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public ClientLocation() {}
    @PrePersist protected void onCreate(){LocalDateTime n=LocalDateTime.now();createdAt=n;updatedAt=n;}
    @PreUpdate protected void onUpdate(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public Client getClient(){return client;} public void setClient(Client v){client=v;}
    public Location getLocation(){return location;} public void setLocation(Location v){location=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
