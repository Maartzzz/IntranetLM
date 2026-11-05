package com.lmoving.intranet.osinterna;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InternalServiceOrderFclRepository extends JpaRepository<InternalServiceOrderFcl, Long> {
    @Query(value = """
        SELECT COALESCE(COUNT(*), 0)
        FROM internal_service_order_fcl f
        JOIN internal_service_orders io ON io.id = f.internal_service_order_id
        LEFT JOIN statuses s ON s.id = io.status_id
        WHERE io.client_service_order_id = :clientOrderId
          AND f.cargo_type_id = :cargoTypeId
          AND (s.name IS NULL OR s.name <> 'Cancelada')
          AND (:excludeId IS NULL OR io.id <> :excludeId)
        """, nativeQuery = true)
    long countDistributedByCargoType(@Param("clientOrderId") Long clientOrderId,
                                      @Param("cargoTypeId") Long cargoTypeId,
                                      @Param("excludeId") Long excludeId);

              
    @EntityGraph(attributePaths = {
        "cargoType",
        "emptyWarehouseLocation"
    })
    List<InternalServiceOrderFcl> findAllByIdIn(List<Long> ids);

}
