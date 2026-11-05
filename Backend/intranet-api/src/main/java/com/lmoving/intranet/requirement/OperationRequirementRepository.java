package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRequirementRepository extends JpaRepository<OperationRequirement, Long> {
    List<OperationRequirement> findAllByOperation_IdOrderByRegisteredAtAsc(Long operationId);
    boolean existsByOperation_IdAndCatalog_Id(Long operationId, Long catalogId);
    List<OperationRequirement> findAllByClientServiceOrder_IdOrderByRegisteredAtAsc(Long clientServiceOrderId);
    List<OperationRequirement> findAllByInternalServiceOrder_IdOrderByRegisteredAtAsc(Long internalServiceOrderId);
    boolean existsByClientServiceOrder_IdAndCatalog_Id(Long clientServiceOrderId, Long catalogId);
    boolean existsByInternalServiceOrder_IdAndCatalog_Id(Long internalServiceOrderId, Long catalogId);
}
