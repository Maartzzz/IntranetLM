package com.lmoving.intranet.oscliente;

import jakarta.persistence.*;

@Entity
@Table(name = "client_service_order_lcl_quantity")
public class ClientServiceOrderLclQuantity {
    @Id
    @Column(name = "client_service_order_id")
    private Long id;
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_service_order_id")
    private ClientServiceOrder clientServiceOrder;
    @Column(name = "total_packages", nullable = false)
    private Integer totalPackages;
    public ClientServiceOrderLclQuantity() {}
    public ClientServiceOrderLclQuantity(ClientServiceOrder order, Integer packages) {
        clientServiceOrder = order; totalPackages = packages;
    }
    public Long getId() { return id; }
    public Integer getTotalPackages() { return totalPackages; }
}
