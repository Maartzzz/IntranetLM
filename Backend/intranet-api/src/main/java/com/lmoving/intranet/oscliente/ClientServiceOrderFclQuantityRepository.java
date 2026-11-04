package com.lmoving.intranet.oscliente;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientServiceOrderFclQuantityRepository extends JpaRepository<ClientServiceOrderFclQuantity, Long> {
    List<ClientServiceOrderFclQuantity> findAllByClientServiceOrder_IdOrderByCargoType_NameAsc(Long orderId);
}
