package com.lmoving.intranet.driver;

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
    public List<DriverResponse> getAllDrivers() {

        return driverService.getAllDrivers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse createDriver(
            @Valid @RequestBody CreateDriverRequest request) {

        return driverService.createDriver(request);
    }

    @PatchMapping("/{username}/active")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @RequestParam boolean active) {

        driverService.setActive(username, active);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{username}")
    public DriverResponse updateDriver(
            @PathVariable String username,
            @Valid @RequestBody UpdateDriverRequest request) {

        return driverService.updateDriver(username, request);
    }

    @GetMapping("/{username}")
    public DriverResponse getDriver(
            @PathVariable String username) {

        return driverService.getDriver(username);
    }
    
}