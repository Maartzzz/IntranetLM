package com.lmoving.intranet.client.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lmoving.intranet.client.dto.ClientRequest;
import com.lmoving.intranet.client.dto.ClientResponse;
import com.lmoving.intranet.client.service.ClientService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTS.VIEW')")
    public ResponseEntity<List<ClientResponse>> listActive() {
        return ResponseEntity.ok(clientService.listActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTS.VIEW')")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PreAuthorize("hasAuthority('CLIENTS.VIEW')")
    @GetMapping("/admin")
    public ResponseEntity<List<ClientResponse>> listAll() {
        return ResponseEntity.ok(clientService.listAll());
    }

    @PreAuthorize("hasAuthority('CLIENTS.CREATE')")
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @PreAuthorize("hasAuthority('CLIENTS.EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PreAuthorize("hasAuthority('CLIENTS.DELETE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ClientResponse> changeStatus(
            @PathVariable Long id, @Valid @RequestBody ClientStatusRequest request) {
        return ResponseEntity.ok(clientService.changeStatus(id, request.active()));
    }

    public record ClientStatusRequest(@NotNull Boolean active) {}
}
