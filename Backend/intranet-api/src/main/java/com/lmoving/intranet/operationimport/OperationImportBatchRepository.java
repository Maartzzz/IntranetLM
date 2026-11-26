package com.lmoving.intranet.operationimport;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationImportBatchRepository extends JpaRepository<OperationImportBatch, Long> {
    List<OperationImportBatch> findTop50ByOrderByCreatedAtDesc();
}
