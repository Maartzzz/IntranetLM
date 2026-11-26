package com.lmoving.intranet.osinterna;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.ClientSubclient;
import com.lmoving.intranet.client.Location;
import com.lmoving.intranet.oscliente.ClientServiceOrder;
import com.lmoving.intranet.status.Status;
import com.lmoving.intranet.provider.Provider;
import com.lmoving.intranet.vehicle.Vehicle;
import jakarta.persistence.*;

@Entity
@Table(name = "internal_service_orders")
public class InternalServiceOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "internal_order_number", nullable = false, unique = true, insertable = false, updatable = false)
    private Long internalOrderNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "client_service_order_id")
    private ClientServiceOrder clientServiceOrder;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "client_subclient_id") private ClientSubclient clientSubclient;
    @Column(name = "client_order_number", length = 80) private String clientOrderNumber;
    @Column(name = "service_type", nullable = false, length = 3) private String serviceType;
    @Column(length = 120) private String reference;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "origin_location_id", nullable = false)
    private Location origin;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "destination_location_id", nullable = false)
    private Location destination;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "provider_id") private Provider provider;
    @Column(name = "scheduled_date") private LocalDate scheduledDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "status_id") private Status status;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "vehicle_id") private Vehicle vehicle;
    @Column(name = "assigned_vehicle_plate_snapshot", length = 20) private String assignedVehiclePlateSnapshot;
    @Column(name = "assigned_vehicle_type_snapshot", length = 20) private String assignedVehicleTypeSnapshot;
    @Column(name = "assigned_trailer_plate_snapshot", length = 20) private String assignedTrailerPlateSnapshot;
    @Column(name = "billing_status", nullable = false, length = 20) private String billingStatus = "PENDIENTE";
    @Column(nullable = false) private boolean active = true;
    @Column(columnDefinition = "TEXT") private String observations;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public InternalServiceOrder() {}
    @PrePersist protected void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getInternalOrderNumber() { return internalOrderNumber; }
    public Client getClient() { return client; } public void setClient(Client v) { client = v; }
    public ClientServiceOrder getClientServiceOrder() { return clientServiceOrder; } public void setClientServiceOrder(ClientServiceOrder v) { clientServiceOrder = v; }
    public ClientSubclient getClientSubclient() { return clientSubclient; } public void setClientSubclient(ClientSubclient v) { clientSubclient = v; }
    public String getClientOrderNumber() { return clientOrderNumber; } public void setClientOrderNumber(String v) { clientOrderNumber = v; }
    public String getServiceType() { return serviceType; } public void setServiceType(String v) { serviceType = v; }
    public String getReference() { return reference; } public void setReference(String v) { reference = v; }
    public Location getOrigin() { return origin; } public void setOrigin(Location v) { origin = v; }
    public Location getDestination() { return destination; } public void setDestination(Location v) { destination = v; }
    public Provider getProvider() { return provider; } public void setProvider(Provider v) { provider = v; }
    public LocalDate getScheduledDate() { return scheduledDate; } public void setScheduledDate(LocalDate v) { scheduledDate = v; }
    public Status getStatus() { return status; } public void setStatus(Status v) { status = v; }
    public Vehicle getVehicle() { return vehicle; } public void setVehicle(Vehicle v) { vehicle = v; }
    public String getAssignedVehiclePlateSnapshot() { return assignedVehiclePlateSnapshot; } public void setAssignedVehiclePlateSnapshot(String v) { assignedVehiclePlateSnapshot = v; }
    public String getAssignedVehicleTypeSnapshot() { return assignedVehicleTypeSnapshot; } public void setAssignedVehicleTypeSnapshot(String v) { assignedVehicleTypeSnapshot = v; }
    public String getAssignedTrailerPlateSnapshot() { return assignedTrailerPlateSnapshot; } public void setAssignedTrailerPlateSnapshot(String v) { assignedTrailerPlateSnapshot = v; }
    public boolean isActive() { return active; } public void setActive(boolean v) { active = v; }
    public String getBillingStatus() { return billingStatus; } public void setBillingStatus(String v) { billingStatus = v; }
    public String getObservations() { return observations; } public void setObservations(String v) { observations = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
