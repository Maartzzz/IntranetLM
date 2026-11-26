package com.lmoving.intranet.operation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    @Query(value = "SELECT nextval('operations_number_seq')", nativeQuery = true)
    Long nextOperationNumber();
    List<Operation> findAllByOrderByCreatedAtDesc();
    List<Operation> findAllByClient_IdOrderByCreatedAtDesc(@Param("clientId") Long clientId);

    boolean existsByOperationNumber(String operationNumber);
    List<Operation> findAllByArchivedFalseOrderByCreatedAtDesc();
    List<Operation> findAllByClient_IdAndArchivedFalseOrderByCreatedAtDesc(Long clientId);
    List<Operation> findAllByArchivedTrueOrderByArchivedAtDesc();
    List<Operation> findAllByArchivedTrueAndStatus_NameIgnoreCaseOrderByArchivedAtDesc(String statusName);

    @Query(value = """
        SELECT CASE WHEN EXISTS (
          SELECT 1
          FROM client_service_orders cso
          JOIN operations o ON o.id = cso.operation_id
          WHERE o.id = :operationId AND cso.active = TRUE
            AND (
              (cso.service_type = 'FCL' AND EXISTS (
                SELECT 1 FROM client_service_order_fcl_quantities q
                WHERE q.client_service_order_id = cso.id
                  AND q.total_quantity > (
                    SELECT COUNT(*) FROM internal_service_order_fcl f
                    JOIN internal_service_orders io ON io.id = f.internal_service_order_id
                    LEFT JOIN statuses st ON st.id = io.status_id
                    WHERE io.client_service_order_id = cso.id
                      AND io.active = TRUE AND (st.name IS NULL OR lower(st.name) <> 'cancelada')
                      AND f.cargo_type_id = q.cargo_type_id
                  )
              ))
              OR
              (cso.service_type = 'LCL' AND EXISTS (
                SELECT 1 FROM client_service_order_lcl_quantity q
                WHERE q.client_service_order_id = cso.id
                  AND q.total_packages > (
                    SELECT COALESCE(SUM(COALESCE(l.packages,0)),0)
                    FROM internal_service_order_lcl l
                    JOIN internal_service_orders io ON io.id = l.internal_service_order_id
                    LEFT JOIN statuses st ON st.id = io.status_id
                    WHERE io.client_service_order_id = cso.id
                      AND io.active = TRUE AND (st.name IS NULL OR lower(st.name) <> 'cancelada')
                  )
              ))
            )
        ) THEN TRUE ELSE FALSE END
        """, nativeQuery = true)
    boolean hasPendingDistribution(@Param("operationId") Long operationId);

}
