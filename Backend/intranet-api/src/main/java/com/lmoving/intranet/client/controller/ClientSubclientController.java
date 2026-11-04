package com.lmoving.intranet.client.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.client.dto.ClientSubclientRequest;
import com.lmoving.intranet.client.dto.ClientSubclientResponse;
import com.lmoving.intranet.client.service.ClientSubclientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class ClientSubclientController {
    private final ClientSubclientService service;
    public ClientSubclientController(ClientSubclientService service) { this.service = service; }
    @GetMapping("/api/clients/{clientId}/subclients") public ResponseEntity<List<ClientSubclientResponse>> list(@PathVariable Long clientId) { return ResponseEntity.ok(service.list(clientId)); }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PostMapping("/api/clients/{clientId}/subclients") public ResponseEntity<ClientSubclientResponse> create(@PathVariable Long clientId, @Valid @RequestBody ClientSubclientRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(clientId, request)); }
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @PatchMapping("/api/client-subclients/{id}/status") public ResponseEntity<ClientSubclientResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) { return ResponseEntity.ok(service.changeStatus(id, request.active())); }
    public record StatusRequest(@NotNull Boolean active) {}
}
