package com.lmoving.intranet.osinterna;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InternalServiceOrderRepository extends JpaRepository<InternalServiceOrder,Long>{
 List<InternalServiceOrder> findAllByOrderByCreatedAtDesc();
 List<InternalServiceOrder> findAllByClient_IdOrderByCreatedAtDesc(Long clientId);
}
