package com.lmoving.intranet.oscliente;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/client-service-orders")
public class ClientServiceOrderController {
    private final ClientServiceOrderService service;
    private final com.lmoving.intranet.osinterna.InternalServiceOrderService internalServiceOrderService;
    public ClientServiceOrderController(ClientServiceOrderService service, com.lmoving.intranet.osinterna.InternalServiceOrderService internalServiceOrderService) { this.service = service; this.internalServiceOrderService = internalServiceOrderService; }

    @GetMapping
    public List<ClientServiceOrderResponse> listar(@RequestParam(required = false) Long clientId) {
        return service.listar(clientId);
    }

    @GetMapping("/{id}")
    public ClientServiceOrderResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    @GetMapping("/{id}/distribution")
    public com.lmoving.intranet.osinterna.InternalServiceOrderService.DistributionResponse obtenerDistribucion(@PathVariable Long id) {
        return internalServiceOrderService.obtenerDistribucion(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public ClientServiceOrderResponse crear(@Valid @RequestBody ClientServiceOrderRequest request) {
        return service.crear(request);
    }
}
