package com.lmoving.intranet.driver;


import org.springframework.security.access.prepost.PreAuthorize;
import com.lmoving.intranet.driver.dto.CreateDriverRequest;
import com.lmoving.intranet.driver.dto.DriverResponse;
import com.lmoving.intranet.driver.dto.UpdateDriverRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(
            DriverService driverService) {

        this.driverService = driverService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DRIVERS.VIEW')")
    public List<DriverResponse> getAllDrivers() {

        return driverService.getAllDrivers();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DRIVERS.CREATE')")
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse createDriver(
            @Valid @RequestBody CreateDriverRequest request) {

        return driverService.createDriver(request);
    }

    @PatchMapping("/{username}/active")
    @PreAuthorize("hasAuthority('DRIVERS.DELETE')")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @RequestParam boolean active) {

        driverService.setActive(username, active);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{username}")
    @PreAuthorize("hasAuthority('DRIVERS.EDIT')")
    public DriverResponse updateDriver(
            @PathVariable String username,
            @Valid @RequestBody UpdateDriverRequest request) {

        return driverService.updateDriver(username, request);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAuthority('DRIVERS.VIEW')")
    public DriverResponse getDriver(
            @PathVariable String username) {

        return driverService.getDriver(username);
    }
    
}