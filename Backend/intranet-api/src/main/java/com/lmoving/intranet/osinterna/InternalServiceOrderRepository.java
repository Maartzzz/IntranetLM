package com.lmoving.intranet.osinterna;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface InternalServiceOrderRepository extends JpaRepository<InternalServiceOrder,Long>{
	boolean existsByClientServiceOrder_IdAndActiveTrue(Long clientServiceOrderId);
	boolean existsByClientServiceOrder_Id(Long clientServiceOrderId);

	List<InternalServiceOrder> findAllByOrderByCreatedAtDesc();

	List<InternalServiceOrder> findAllByClient_IdOrderByCreatedAtDesc(
		Long clientId
	);

	@Query("""
		SELECT o FROM InternalServiceOrder o
		LEFT JOIN FETCH o.client
		LEFT JOIN FETCH o.clientServiceOrder
		LEFT JOIN FETCH o.origin
		LEFT JOIN FETCH o.destination
		LEFT JOIN FETCH o.provider
		LEFT JOIN FETCH o.status
		WHERE o.active = true AND o.scheduledDate < :today
		  AND o.status IS NOT NULL
		  AND LOWER(o.status.name) = 'vencida'
		ORDER BY o.scheduledDate ASC, o.internalOrderNumber ASC
	""")
	List<InternalServiceOrder> findOverdue(@Param("today") LocalDate today);

	List<InternalServiceOrder> findAllByScheduledDateBeforeAndActiveTrueAndStatus_Id(
		LocalDate today, Long statusId
	);


	@Query("""
		SELECT COUNT(o) FROM InternalServiceOrder o
		WHERE o.active = true
		  AND o.status IS NOT NULL
		  AND LOWER(o.status.name) = 'completada'
	""")
	long countActiveCompletedOrders();

	long countByActiveTrueAndStatus_NameIgnoreCase(String statusName);

	@Query("""
		SELECT COUNT(o) FROM InternalServiceOrder o
		WHERE o.active = true
		  AND o.scheduledDate IS NOT NULL
		  AND o.scheduledDate < :today
		  AND (o.status IS NULL OR o.status.finalStatus = false)
	""")
	long countOverdueOrders(@Param("today") LocalDate today);

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
		WHERE o.active = true
			AND o.scheduledDate BETWEEN :startDate AND :endDate
			AND (
				o.status IS NULL
				OR o.status.finalStatus = false
				OR LOWER(o.status.name) = 'completada'
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
		WHERE o.active = true
		AND (
			o.scheduledDate IS NULL
			OR o.scheduledDate < :startDate
			OR o.scheduledDate > :endDate
		)
		AND (
			o.status IS NULL
			OR o.status.finalStatus = false
		)
		ORDER BY o.scheduledDate ASC NULLS FIRST, o.internalOrderNumber ASC
	""")
	List<InternalServiceOrder> findAvailableForPeriod(
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	List<InternalServiceOrder> findAllByClientServiceOrder_IdOrderByCreatedAtDesc(
		Long clientServiceOrderId
	);
}
