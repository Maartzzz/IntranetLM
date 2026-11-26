package com.lmoving.intranet.vehicle.assignment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverVehicleAssignmentRepository extends JpaRepository<DriverVehicleAssignment, Long> {
    List<DriverVehicleAssignment> findAllByOrderByStartAtDescIdDesc();
    List<DriverVehicleAssignment> findAllByEndAtIsNullOrderByStartAtDescIdDesc();
    boolean existsByDriver_IdAndVehicle_IdAndEndAtIsNull(Long driverId, Long vehicleId);
    boolean existsByVehicle_IdAndEndAtIsNullAndDriver_IdNot(Long vehicleId, Long driverId);
    boolean existsByDriver_IdAndEndAtIsNullAndVehicle_IdNot(Long driverId, Long vehicleId);
}
