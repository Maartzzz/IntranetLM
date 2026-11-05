package com.lmoving.intranet.osinterna;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface InternalServiceOrderRepository extends JpaRepository<InternalServiceOrder,Long>{
	
	List<InternalServiceOrder> findAllByOrderByCreatedAtDesc();

	List<InternalServiceOrder> findAllByClient_IdOrderByCreatedAtDesc(
		Long clientId
	);

	// O/S programadas dentro del periodo
	@Query("""
		SELECT o
		FROM InternalServiceOrder o
		LEFT JOIN FETCH o.client
		LEFT JOIN FETCH o.clientServiceOrder
		LEFT JOIN FETCH o.origin
		LEFT JOIN FETCH o.destination
		LEFT JOIN FETCH o.provider
		LEFT JOIN FETCH o.status
		WHERE o.scheduledDate BETWEEN :startDate AND :endDate
			AND (
				o.status IS NULL
				OR LOWER(o.status.name) <> 'cancelada'
			)
		ORDER BY o.scheduledDate ASC, o.internalOrderNumber ASC
	""")
	List<InternalServiceOrder> findScheduledForPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	// O/S sin fecha o fuera del periodo seleccionado
	@Query("""
		SELECT o
		FROM InternalServiceOrder o
		LEFT JOIN FETCH o.client
		LEFT JOIN FETCH o.clientServiceOrder
		LEFT JOIN FETCH o.origin
		LEFT JOIN FETCH o.destination
		LEFT JOIN FETCH o.provider
		LEFT JOIN FETCH o.status
		WHERE (
			o.scheduledDate IS NULL
			OR o.scheduledDate < :startDate
			OR o.scheduledDate > :endDate
		)
		AND (
			o.status IS NULL
			OR LOWER(o.status.name) <> 'cancelada'
		)
		ORDER BY o.scheduledDate ASC NULLS FIRST, o.internalOrderNumber ASC
	""")
	List<InternalServiceOrder> findAvailableForPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);
}
