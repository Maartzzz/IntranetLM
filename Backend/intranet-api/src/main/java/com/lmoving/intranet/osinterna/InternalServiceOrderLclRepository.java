package com.lmoving.intranet.osinterna;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InternalServiceOrderLclRepository extends JpaRepository<InternalServiceOrderLcl, Long> {
    @Query(value = """
        SELECT COALESCE(SUM(COALESCE(l.packages, 0)), 0)
        FROM internal_service_order_lcl l
        JOIN internal_service_orders io ON io.id = l.internal_service_order_id
        LEFT JOIN statuses s ON s.id = io.status_id
        WHERE io.client_service_order_id = :clientOrderId
          AND (s.name IS NULL OR s.name <> 'Cancelada')
          AND (:excludeId IS NULL OR io.id <> :excludeId)
        """, nativeQuery = true)
    long sumDistributedPackages(@Param("clientOrderId") Long clientOrderId,
                                @Param("excludeId") Long excludeId);

    List<InternalServiceOrderLcl> findAllByIdIn(List<Long> ids);
}
