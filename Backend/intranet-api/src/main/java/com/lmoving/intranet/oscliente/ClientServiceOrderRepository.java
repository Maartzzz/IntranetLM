package com.lmoving.intranet.oscliente;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientServiceOrderRepository extends JpaRepository<ClientServiceOrder, Long> {
    List<ClientServiceOrder> findAllByOrderByCreatedAtDesc();
    List<ClientServiceOrder> findAllByClient_IdOrderByCreatedAtDesc(Long clientId);
    boolean existsByClient_IdAndClientOrderNumberIgnoreCase(Long clientId, String clientOrderNumber);
    boolean existsByClient_IdAndClientOrderNumberIgnoreCaseAndIdNot(Long clientId, String clientOrderNumber, Long id);
}
