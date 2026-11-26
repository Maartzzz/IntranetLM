package com.lmoving.intranet.vehicle.assignment;

import java.time.LocalDateTime;
import java.util.List;

public final class DriverVehicleAssignmentDtos {
    private DriverVehicleAssignmentDtos() {}

    public record AssignmentRequest(Long driverId, Long vehicleId, LocalDateTime startAt,
            String reason, String observations) {}

    public record DriverOption(Long id, String username, String fullName, String licenseNumber) {}

    public record AssignmentResponse(Long id, Long driverId, String driverUsername,
            String driverName, String licenseNumber, Long vehicleId, String vehiclePlate,
            String vehicleType, LocalDateTime startAt, LocalDateTime endAt,
            String reason, String observations, List<String> warnings) {}
}
