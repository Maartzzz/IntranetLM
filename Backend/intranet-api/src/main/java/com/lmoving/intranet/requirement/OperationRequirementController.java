package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.requirement.dto.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/operations/{operationId}/requirements")
public class OperationRequirementController {
    private final OperationRequirementService service;
    public OperationRequirementController(OperationRequirementService service) { this.service = service; }

    @GetMapping
    public List<OperationRequirementResponse> listar(@PathVariable Long operationId) {
        return service.listar(operationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public OperationRequirementResponse crear(@PathVariable Long operationId,
            @Valid @RequestBody OperationRequirementRequest request) {
        return service.crear(operationId, request);
    }

    @PutMapping("/{requirementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public OperationRequirementResponse actualizar(@PathVariable Long operationId,
            @PathVariable Long requirementId, @Valid @RequestBody OperationRequirementRequest request) {
        return service.actualizar(operationId, requirementId, request);
    }
}
