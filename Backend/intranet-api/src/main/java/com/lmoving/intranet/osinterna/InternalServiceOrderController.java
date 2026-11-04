package com.lmoving.intranet.osinterna;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
@RestController @RequestMapping("/api/internal-service-orders")
public class InternalServiceOrderController {
 private final InternalServiceOrderService service;
 public InternalServiceOrderController(InternalServiceOrderService service){this.service=service;}
 @GetMapping public List<InternalServiceOrderResponse> listar(@RequestParam(required=false) Long clientId){return service.listar(clientId);}
 @GetMapping("/{id}") public InternalServiceOrderResponse obtener(@PathVariable Long id){return service.obtener(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
 public InternalServiceOrderResponse crear(@Valid @RequestBody InternalServiceOrderRequest request){return service.crear(request);}
}
