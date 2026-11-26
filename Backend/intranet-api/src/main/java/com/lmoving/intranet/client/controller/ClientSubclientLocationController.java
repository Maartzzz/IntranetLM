package com.lmoving.intranet.client.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.client.dto.ClientSubclientLocationRequest;
import com.lmoving.intranet.client.dto.ClientSubclientLocationResponse;
import com.lmoving.intranet.client.service.ClientSubclientLocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class ClientSubclientLocationController {
    private final ClientSubclientLocationService service;
    public ClientSubclientLocationController(ClientSubclientLocationService service) { this.service = service; }
    @GetMapping("/api/client-subclients/{relationId}/locations") @PreAuthorize("hasAuthority('LOCATIONS.VIEW')") public ResponseEntity<List<ClientSubclientLocationResponse>> list(@PathVariable Long relationId) { return ResponseEntity.ok(service.list(relationId)); }
    @PreAuthorize("hasAuthority('LOCATIONS.VIEW')") @GetMapping("/api/client-subclients/{relationId}/locations/admin") public ResponseEntity<List<ClientSubclientLocationResponse>> listAll(@PathVariable Long relationId) { return ResponseEntity.ok(service.listAll(relationId)); }
    @PreAuthorize("hasAuthority('LOCATIONS.EDIT')") @PostMapping("/api/client-subclients/{relationId}/locations") public ResponseEntity<ClientSubclientLocationResponse> add(@PathVariable Long relationId, @Valid @RequestBody ClientSubclientLocationRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.add(relationId, request)); }
    @PreAuthorize("hasAuthority('LOCATIONS.EDIT')") @PutMapping("/api/client-subclient-locations/{id}") public ResponseEntity<ClientSubclientLocationResponse> update(@PathVariable Long id, @Valid @RequestBody ClientSubclientLocationRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @PreAuthorize("hasAuthority('LOCATIONS.DELETE')") @PatchMapping("/api/client-subclient-locations/{id}/status") public ResponseEntity<ClientSubclientLocationResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) { return ResponseEntity.ok(service.changeStatus(id, request.active())); }
    public record StatusRequest(@NotNull Boolean active) {}
}
