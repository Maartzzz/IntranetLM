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
}
