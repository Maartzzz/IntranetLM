package com.lmoving.intranet.provider.controller;

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
import com.lmoving.intranet.provider.dto.ProviderRequest;
import com.lmoving.intranet.provider.dto.ProviderResponse;
import com.lmoving.intranet.provider.dto.ProviderStatusRequest;
import com.lmoving.intranet.provider.service.ProviderService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
    private final ProviderService service;
    public ProviderController(ProviderService service) { this.service = service; }

    @PreAuthorize("hasAuthority('CONFIGURATION.PROVIDERS.VIEW')")
    @GetMapping
    public ResponseEntity<List<ProviderResponse>> listActive() { return ResponseEntity.ok(service.listActive()); }

    @PreAuthorize("hasAuthority('CONFIGURATION.PROVIDERS.VIEW')")
    @GetMapping("/admin")
    public ResponseEntity<List<ProviderResponse>> listAll() { return ResponseEntity.ok(service.listAll()); }

    @PreAuthorize("hasAuthority('CONFIGURATION.PROVIDERS.VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @PreAuthorize("hasAuthority('CONFIGURATION.PROVIDERS.CREATE')")
    @PostMapping
    public ResponseEntity<ProviderResponse> create(@Valid @RequestBody ProviderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.PROVIDERS.EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> update(@PathVariable Long id, @Valid @RequestBody ProviderRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAuthority('CONFIGURATION.PROVIDERS.DELETE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProviderResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody ProviderStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request.active()));
    }
}
