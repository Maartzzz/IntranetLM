package com.lmoving.intranet.vehicle.assignment;

import com.lmoving.intranet.audit.AuditLogService;
import com.lmoving.intranet.driver.Driver;
import com.lmoving.intranet.driver.DriverRepository;
import com.lmoving.intranet.vehicle.Vehicle;
import com.lmoving.intranet.vehicle.VehicleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DriverVehicleAssignmentService {
    private final DriverVehicleAssignmentRepository assignments;
    private final DriverRepository drivers;
    private final VehicleRepository vehicles;
    private final AuditLogService audit;

    public DriverVehicleAssignmentService(DriverVehicleAssignmentRepository assignments,
            DriverRepository drivers, VehicleRepository vehicles, AuditLogService audit) {
        this.assignments = assignments;
        this.drivers = drivers;
        this.vehicles = vehicles;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<DriverVehicleAssignmentDtos.DriverOption> driverOptions() {
        return drivers.findAll().stream()
                .filter(Driver::isActive)
                .filter(d -> d.getUser() != null && d.getUser().isActive())
                .map(d -> new DriverVehicleAssignmentDtos.DriverOption(
                        d.getId(), d.getUser().getUsername(),
                        (d.getUser().getFirstName() + " " + d.getUser().getLastName()).trim(),
                        d.getLicenseNumber()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DriverVehicleAssignmentDtos.AssignmentResponse> history(boolean activeOnly) {
        List<DriverVehicleAssignment> rows = activeOnly
                ? assignments.findAllByEndAtIsNullOrderByStartAtDescIdDesc()
                : assignments.findAllByOrderByStartAtDescIdDesc();
        return rows.stream().map(a -> response(a, List.of())).toList();
    }

    public DriverVehicleAssignmentDtos.AssignmentResponse create(
            DriverVehicleAssignmentDtos.AssignmentRequest request) {
        if (request.driverId() == null || request.vehicleId() == null)
            throw bad("Debe seleccionar un chofer y un vehículo");

        Driver driver = drivers.findById(request.driverId())
                .orElseThrow(() -> missing("Chofer no encontrado"));
        Vehicle vehicle = vehicles.findById(request.vehicleId())
                .orElseThrow(() -> missing("Vehículo no encontrado"));

        if (!driver.isActive() || driver.getUser() == null || !driver.getUser().isActive())
            throw bad("El chofer debe estar activo para recibir asignaciones");
        if (!vehicle.isActive())
            throw bad("El vehículo debe estar activo para recibir asignaciones");
        if (assignments.existsByDriver_IdAndVehicle_IdAndEndAtIsNull(driver.getId(), vehicle.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una asignación vigente de este chofer a este vehículo");

        LocalDateTime start = request.startAt() == null ? LocalDateTime.now() : request.startAt();
        List<String> warnings = new ArrayList<>();
        if (assignments.existsByVehicle_IdAndEndAtIsNullAndDriver_IdNot(vehicle.getId(), driver.getId()))
            warnings.add("El vehículo ya tiene otro chofer con asignación vigente. Se permite mantener ambas asignaciones.");
        if (assignments.existsByDriver_IdAndEndAtIsNullAndVehicle_IdNot(driver.getId(), vehicle.getId()))
            warnings.add("El chofer ya tiene otro vehículo con asignación vigente. Se permite mantener ambas asignaciones.");

        DriverVehicleAssignment entity = new DriverVehicleAssignment();
        entity.setDriver(driver);
        entity.setVehicle(vehicle);
        entity.setStartAt(start);
        entity.setReason(request.reason() == null || request.reason().isBlank()
                ? "ASIGNACION" : request.reason().trim());
        entity.setObservations(request.observations());
        DriverVehicleAssignment saved = assignments.save(entity);
        audit.registrar("FLOTA", "ASIGNACION_CHOFER_VEHICULO", saved.getId(), "CREATED",
                "Se asignó el chofer " + driver.getUser().getUsername() + " al vehículo " + vehicle.getPlate(),
                null, driver.getUser().getUsername() + " / " + vehicle.getPlate() + " / " + start);
        return response(saved, warnings);
    }

    public DriverVehicleAssignmentDtos.AssignmentResponse close(Long id, LocalDateTime endAt) {
        DriverVehicleAssignment assignment = assignments.findById(id)
                .orElseThrow(() -> missing("Asignación no encontrada"));
        if (assignment.getEndAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La asignación ya se encuentra cerrada");
        LocalDateTime end = endAt == null ? LocalDateTime.now() : endAt;
        if (end.isBefore(assignment.getStartAt()))
            throw bad("La fecha de término no puede ser anterior a la fecha de inicio");
        assignment.setEndAt(end);
        DriverVehicleAssignment saved = assignments.save(assignment);
        audit.registrar("FLOTA", "ASIGNACION_CHOFER_VEHICULO", saved.getId(), "CLOSED",
                "Se cerró la asignación del chofer " + saved.getDriver().getUser().getUsername()
                        + " al vehículo " + saved.getVehicle().getPlate(),
                null, end.toString());
        return response(saved, List.of());
    }

    private DriverVehicleAssignmentDtos.AssignmentResponse response(
            DriverVehicleAssignment a, List<String> warnings) {
        Driver d = a.getDriver();
        String name = (d.getUser().getFirstName() + " " + d.getUser().getLastName()).trim();
        return new DriverVehicleAssignmentDtos.AssignmentResponse(a.getId(), d.getId(),
                d.getUser().getUsername(), name, d.getLicenseNumber(), a.getVehicle().getId(),
                a.getVehicle().getPlate(), a.getVehicle().getType().name(), a.getStartAt(),
                a.getEndAt(), a.getReason(), a.getObservations(), warnings);
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    private ResponseStatusException missing(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
