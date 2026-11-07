package com.lmoving.intranet.operation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.ClientSubclient;
import com.lmoving.intranet.status.Status;
import jakarta.persistence.*;

@Entity
@Table(name = "operations")
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_number", nullable = false, unique = true, updatable = false, length = 20)
    private String operationNumber;

    @Column(length = 150)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_subclient_id")
    private ClientSubclient clientSubclient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_type_id", nullable = false)
    private CargoType cargoType;

    @Column(name = "customer_order_number", length = 100)
    private String customerOrderNumber;

    @Column(length = 200)
    private String origin;

    @Column(length = 200)
    private String destination;

    private Integer quantity;

    @Column(name = "estimated_date")
    private LocalDate estimatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @Column(length = 1000)
    private String observation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Operation() {}
    @PrePersist protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getOperationNumber() { return operationNumber; }
    public void setOperationNumber(String v) { operationNumber = v; }
    public String getReference() { return reference; }
    public void setReference(String v) { reference = v; }
    public Client getClient() { return client; }
    public void setClient(Client v) { client = v; }
    public ClientSubclient getClientSubclient() { return clientSubclient; }
    public void setClientSubclient(ClientSubclient v) { clientSubclient = v; }
    public CargoType getCargoType() { return cargoType; }
    public void setCargoType(CargoType v) { cargoType = v; }
    public String getCustomerOrderNumber() { return customerOrderNumber; }
    public void setCustomerOrderNumber(String v) { customerOrderNumber = v; }
    public String getOrigin() { return origin; }
    public void setOrigin(String v) { origin = v; }
    public String getDestination() { return destination; }
    public void setDestination(String v) { destination = v; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer v) { quantity = v; }
    public LocalDate getEstimatedDate() { return estimatedDate; }
    public void setEstimatedDate(LocalDate v) { estimatedDate = v; }
    public Status getStatus() { return status; }
    public void setStatus(Status v) { status = v; }
    public String getObservation() { return observation; }
    public void setObservation(String v) { observation = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
