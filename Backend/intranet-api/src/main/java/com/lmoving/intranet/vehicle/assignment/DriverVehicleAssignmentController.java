package com.lmoving.intranet.vehicle.assignment;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle-driver-assignments")
public class DriverVehicleAssignmentController {
    private final DriverVehicleAssignmentService service;

    public DriverVehicleAssignmentController(DriverVehicleAssignmentService service) {
        this.service = service;
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('FLEET.VIEW')")
    public List<DriverVehicleAssignmentDtos.DriverOption> drivers() {
        return service.driverOptions();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FLEET.VIEW')")
    public List<DriverVehicleAssignmentDtos.AssignmentResponse> history(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.history(activeOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FLEET.EDIT')")
    public DriverVehicleAssignmentDtos.AssignmentResponse create(
            @RequestBody DriverVehicleAssignmentDtos.AssignmentRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAuthority('FLEET.EDIT')")
    public DriverVehicleAssignmentDtos.AssignmentResponse close(
            @PathVariable Long id, @RequestParam(required = false) LocalDateTime endAt) {
        return service.close(id, endAt);
    }
}
