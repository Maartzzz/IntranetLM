package com.lmoving.intranet.osinterna.schedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface InternalServiceOrderScheduleVersionRepository extends JpaRepository<InternalServiceOrderScheduleVersion, Long> {
 @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM InternalServiceOrderScheduleVersion v WHERE v.startDate = :startDate AND v.endDate = :endDate")
 Integer findMaxVersionNumber(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
 List<InternalServiceOrderScheduleVersion> findAllByStartDateAndEndDateOrderByVersionNumberDesc(LocalDate startDate, LocalDate endDate);
}
