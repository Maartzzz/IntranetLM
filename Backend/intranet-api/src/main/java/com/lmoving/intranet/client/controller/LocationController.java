package com.lmoving.intranet.client.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.client.dto.LocationRequest;
import com.lmoving.intranet.client.dto.LocationResponse;
import com.lmoving.intranet.client.dto.LocationSaveResponse;
import com.lmoving.intranet.client.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService service;
    public LocationController(LocationService service) { this.service = service; }
    @GetMapping @PreAuthorize("hasAuthority('LOCATIONS.VIEW')") public ResponseEntity<List<LocationResponse>> listActive() { return ResponseEntity.ok(service.listActive()); }
    @PreAuthorize("hasAuthority('LOCATIONS.VIEW')") @GetMapping("/admin") public ResponseEntity<List<LocationResponse>> listAll() { return ResponseEntity.ok(service.listAll()); }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('LOCATIONS.VIEW')") public ResponseEntity<LocationResponse> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }
    @PreAuthorize("hasAuthority('LOCATIONS.CREATE')") @PostMapping public ResponseEntity<LocationSaveResponse> create(@Valid @RequestBody LocationRequest request) { LocationSaveResponse result = service.create(request); return ResponseEntity.status(result.reused() ? HttpStatus.OK : HttpStatus.CREATED).body(result); }
    @PreAuthorize("hasAuthority('LOCATIONS.EDIT')") @PutMapping("/{id}") public ResponseEntity<LocationResponse> update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @PreAuthorize("hasAuthority('LOCATIONS.DELETE')") @PatchMapping("/{id}/status") public ResponseEntity<LocationResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) { return ResponseEntity.ok(service.changeStatus(id, request.active())); }
    public record StatusRequest(@NotNull Boolean active) {}
}
