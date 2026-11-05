package com.lmoving.intranet.oscliente;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientServiceOrderRepository extends JpaRepository<ClientServiceOrder, Long> {
    List<ClientServiceOrder> findAllByOrderByCreatedAtDesc();
    List<ClientServiceOrder> findAllByClient_IdOrderByCreatedAtDesc(Long clientId);
    boolean existsByClient_IdAndClientOrderNumberIgnoreCase(Long clientId, String clientOrderNumber);
    boolean existsByClient_IdAndClientOrderNumberIgnoreCaseAndIdNot(Long clientId, String clientOrderNumber, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ClientServiceOrder o where o.id = :id")
    Optional<ClientServiceOrder> findByIdForUpdate(@Param("id") Long id);
}
