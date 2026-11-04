package com.lmoving.intranet.oscliente;

import com.lmoving.intranet.cargotype.CargoType;
import jakarta.persistence.*;

@Entity
@Table(name = "client_service_order_fcl_quantities", uniqueConstraints = @UniqueConstraint(
    name = "uq_client_service_order_fcl_type", columnNames = {"client_service_order_id", "cargo_type_id"}))
public class ClientServiceOrderFclQuantity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_service_order_id", nullable = false)
    private ClientServiceOrder clientServiceOrder;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_type_id", nullable = false)
    private CargoType cargoType;
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;
    public ClientServiceOrderFclQuantity() {}
    public ClientServiceOrderFclQuantity(ClientServiceOrder order, CargoType type, Integer quantity) {
        clientServiceOrder = order; cargoType = type; totalQuantity = quantity;
    }
    public Long getId() { return id; }
    public ClientServiceOrder getClientServiceOrder() { return clientServiceOrder; }
    public CargoType getCargoType() { return cargoType; }
    public Integer getTotalQuantity() { return totalQuantity; }
}
