package com.lmoving.intranet.activity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationActivityRepository extends JpaRepository<OperationActivity, Long> {
    List<OperationActivity> findAllByOperation_IdOrderByCreatedAtDescIdDesc(Long operationId);
}
