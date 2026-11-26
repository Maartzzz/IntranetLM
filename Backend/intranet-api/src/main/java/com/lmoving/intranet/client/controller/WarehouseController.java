package com.lmoving.intranet.client.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.client.dto.WarehouseRequest;
import com.lmoving.intranet.client.dto.WarehouseResponse;
import com.lmoving.intranet.client.service.WarehouseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseService service;
    public WarehouseController(WarehouseService service) { this.service = service; }
    @GetMapping @PreAuthorize("hasAuthority('WAREHOUSES.VIEW')") public ResponseEntity<List<WarehouseResponse>> listActive() { return ResponseEntity.ok(service.listActive()); }
    @PreAuthorize("hasAuthority('WAREHOUSES.VIEW')") @GetMapping("/admin") public ResponseEntity<List<WarehouseResponse>> listAll() { return ResponseEntity.ok(service.listAll()); }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('WAREHOUSES.VIEW')") public ResponseEntity<WarehouseResponse> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }
    @PreAuthorize("hasAuthority('WAREHOUSES.CREATE')") @PostMapping public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @PreAuthorize("hasAuthority('WAREHOUSES.EDIT')") @PutMapping("/{id}") public ResponseEntity<WarehouseResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @PreAuthorize("hasAuthority('WAREHOUSES.DELETE')") @PatchMapping("/{id}/status") public ResponseEntity<WarehouseResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) { return ResponseEntity.ok(service.changeStatus(id, request.active())); }
    public record StatusRequest(@NotNull Boolean active) {}
}
