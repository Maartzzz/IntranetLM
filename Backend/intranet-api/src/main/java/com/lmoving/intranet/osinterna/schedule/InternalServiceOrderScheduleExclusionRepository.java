package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InternalServiceOrderScheduleExclusionRepository
    extends JpaRepository<InternalServiceOrderScheduleExclusion, Long> {

    boolean existsByInternalServiceOrder_IdAndExcludedDate(
        Long internalServiceOrderId,
        LocalDate excludedDate
    );

    List<InternalServiceOrderScheduleExclusion> findAllByExcludedDate(
        LocalDate excludedDate
    );

    void deleteByInternalServiceOrder_IdAndExcludedDate(
        Long internalServiceOrderId,
        LocalDate excludedDate
    );

    List<InternalServiceOrderScheduleExclusion> findAllByExcludedDateBetween(
        LocalDate startDate,
        LocalDate endDate
    );

    void deleteAllByInternalServiceOrder_Id(Long internalServiceOrderId);
}