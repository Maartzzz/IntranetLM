package com.lmoving.intranet.osinterna.schedule;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternalServiceOrderScheduleHistoryRepository
        extends JpaRepository<InternalServiceOrderScheduleHistory, Long> {
    List<InternalServiceOrderScheduleHistory> findAllByInternalServiceOrder_IdOrderByCreatedAtDescIdDesc(Long orderId);
}
