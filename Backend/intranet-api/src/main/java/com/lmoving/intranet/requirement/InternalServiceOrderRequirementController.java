package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.requirement.dto.OperationRequirementRequest;
import com.lmoving.intranet.requirement.dto.OperationRequirementResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/internal-service-orders/{orderId}/requirements")
public class InternalServiceOrderRequirementController {
    private final OperationRequirementService service;
    public InternalServiceOrderRequirementController(OperationRequirementService service) { this.service = service; }

    @GetMapping
    public List<OperationRequirementResponse> listar(@PathVariable Long orderId) {
        return service.listarOssInterna(orderId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public OperationRequirementResponse crear(@PathVariable Long orderId,
            @Valid @RequestBody OperationRequirementRequest request) {
        return service.crearOssInterna(orderId, request);
    }

    @PutMapping("/{requirementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public OperationRequirementResponse actualizar(@PathVariable Long orderId,
            @PathVariable Long requirementId, @Valid @RequestBody OperationRequirementRequest request) {
        return service.actualizarOssInterna(orderId, requirementId, request);
    }
}
