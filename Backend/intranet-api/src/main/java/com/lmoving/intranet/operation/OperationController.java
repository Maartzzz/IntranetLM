package com.lmoving.intranet.operation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/operations")
public class OperationController {
    private final OperationService operationService;
    public OperationController(OperationService operationService) { this.operationService = operationService; }

    @GetMapping
    @PreAuthorize("hasAuthority('OPERATIONS.VIEW')")
    public List<OperationResponse> listar(@RequestParam(required = false) Long clientId) {
        return clientId == null ? operationService.listarTodas() : operationService.listarPorCliente(clientId);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATIONS.VIEW')")
    public OperationResponse obtener(@PathVariable Long id) { return operationService.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('OPERATIONS.CREATE')")
    public OperationResponse crear(@Valid @RequestBody OperationRequest request) {
        return operationService.crear(request);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATIONS.EDIT')")
    public OperationResponse actualizar(@PathVariable Long id, @Valid @RequestBody OperationRequest request) {
        return operationService.actualizar(id, request);
    }
}
