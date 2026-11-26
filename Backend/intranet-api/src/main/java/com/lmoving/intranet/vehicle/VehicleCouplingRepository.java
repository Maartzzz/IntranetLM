package com.lmoving.intranet.vehicle; import java.time.LocalDateTime; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface VehicleCouplingRepository extends JpaRepository<VehicleCoupling,Long>{
 Optional<VehicleCoupling> findFirstByTractor_IdAndEndAtIsNullOrderByStartAtDesc(Long id);
 Optional<VehicleCoupling> findFirstByTrailer_IdAndEndAtIsNullOrderByStartAtDesc(Long id);
 List<VehicleCoupling> findAllByOrderByStartAtDesc();
 boolean existsByTractor_IdAndEndAtIsNull(Long id); boolean existsByTrailer_IdAndEndAtIsNull(Long id);
}