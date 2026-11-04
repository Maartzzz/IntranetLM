package com.lmoving.intranet.oscliente;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.operation.Operation;
import com.lmoving.intranet.status.Status;
import jakarta.persistence.*;

@Entity
@Table(name = "client_service_orders")
public class ClientServiceOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_order_number", nullable = false, length = 80)
    private String clientOrderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private Operation operation;

    @Column(length = 120)
    private String reference;

    @Column(name = "service_type", nullable = false, length = 3)
    private String serviceType;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "estimated_arrival_date")
    private LocalDate estimatedArrivalDate;

    @Column(length = 200)
    private String origin;

    @Column(length = 200)
    private String destination;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ClientServiceOrder() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getClientOrderNumber() { return clientOrderNumber; }
    public void setClientOrderNumber(String value) { clientOrderNumber = value; }
    public Client getClient() { return client; }
    public void setClient(Client value) { client = value; }
    public Operation getOperation() { return operation; }
    public void setOperation(Operation value) { operation = value; }
    public String getReference() { return reference; }
    public void setReference(String value) { reference = value; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String value) { serviceType = value; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate value) { receivedDate = value; }
    public LocalDate getEstimatedArrivalDate() { return estimatedArrivalDate; }
    public void setEstimatedArrivalDate(LocalDate value) { estimatedArrivalDate = value; }
    public String getOrigin() { return origin; }
    public void setOrigin(String value) { origin = value; }
    public String getDestination() { return destination; }
    public void setDestination(String value) { destination = value; }
    public String getObservation() { return observation; }
    public void setObservation(String value) { observation = value; }
    public Status getStatus() { return status; }
    public void setStatus(Status value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
