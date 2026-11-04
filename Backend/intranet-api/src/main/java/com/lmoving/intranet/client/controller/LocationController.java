package com.lmoving.intranet.client.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.client.dto.LocationRequest;
import com.lmoving.intranet.client.dto.LocationResponse;
import com.lmoving.intranet.client.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService service;
    public LocationController(LocationService service) { this.service = service; }
    @GetMapping public ResponseEntity<List<LocationResponse>> listActive() { return ResponseEntity.ok(service.listActive()); }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')") @GetMapping("/admin") public ResponseEntity<List<LocationResponse>> listAll() { return ResponseEntity.ok(service.listAll()); }
    @GetMapping("/{id}") public ResponseEntity<LocationResponse> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')") @PostMapping public ResponseEntity<LocationResponse> create(@Valid @RequestBody LocationRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')") @PutMapping("/{id}") public ResponseEntity<LocationResponse> update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')") @PatchMapping("/{id}/status") public ResponseEntity<LocationResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) { return ResponseEntity.ok(service.changeStatus(id, request.active())); }
    public record StatusRequest(@NotNull Boolean active) {}
}
