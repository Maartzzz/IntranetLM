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
    public ClientServiceOrderController(ClientServiceOrderService service) { this.service = service; }

    @GetMapping
    public List<ClientServiceOrderResponse> listar(@RequestParam(required = false) Long clientId) {
        return service.listar(clientId);
    }

    @GetMapping("/{id}")
    public ClientServiceOrderResponse obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public ClientServiceOrderResponse crear(@Valid @RequestBody ClientServiceOrderRequest request) {
        return service.crear(request);
    }
}
