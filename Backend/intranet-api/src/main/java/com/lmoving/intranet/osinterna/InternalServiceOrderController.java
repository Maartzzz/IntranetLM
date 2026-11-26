package com.lmoving.intranet.osinterna;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleService;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleIncludeRequest;

@RestController
@RequestMapping("/api/internal-service-orders")
public class InternalServiceOrderController {
    private final InternalServiceOrderService service;
    private final com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleHistoryService scheduleHistoryService;
    private final InternalServiceOrderScheduleService scheduleService;
    public InternalServiceOrderController(InternalServiceOrderService service,
            com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleHistoryService scheduleHistoryService,
            InternalServiceOrderScheduleService scheduleService) {
        this.service = service;
        this.scheduleHistoryService = scheduleHistoryService;
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.VIEW')")
    public List<InternalServiceOrderResponse> listar(@RequestParam(required=false) Long clientId){return service.listar(clientId);}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.VIEW')")
    public InternalServiceOrderResponse obtener(@PathVariable Long id){return service.obtener(id);}

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.EDIT')")
    public InternalServiceOrderResponse actualizar(@PathVariable Long id,@Valid @RequestBody InternalServiceOrderRequest request){return service.actualizar(id,request);}


    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.DELETE')")
    public InternalServiceOrderResponse cambiarActivo(@PathVariable Long id, @RequestParam boolean active) {
        return service.cambiarActivo(id, active);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.DELETE')")
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.CREATE')")
    public List<InternalServiceOrderResponse> crearLote(@Valid @RequestBody List<@Valid InternalServiceOrderRequest> requests) {
        return service.crearLote(requests);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.CREATE')")
    public InternalServiceOrderResponse crear(@Valid @RequestBody InternalServiceOrderRequest request){return service.crear(request);}


    @GetMapping("/schedule/completed-count")
    @PreAuthorize("hasAuthority('SCHEDULING.VIEW')")
    public long contarOrdenesCompletadas() {
        return scheduleService.contarOrdenesCompletadas();
    }

    @GetMapping("/schedule/global-summary")
    @PreAuthorize("hasAuthority('SCHEDULING.VIEW')")
    public InternalServiceOrderScheduleService.ScheduleGlobalSummaryResponse obtenerResumenGlobal() {
        return scheduleService.obtenerResumenGlobal();
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasAuthority('SCHEDULING.VIEW')")
    public InternalServiceOrderScheduleService.ScheduleBoardResponse obtenerTablero(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return scheduleService.obtenerTablero(startDate, endDate);
    }

    @PostMapping("/{id}/schedule/exclude")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCHEDULING.EDIT')")
    public void retirarDelTablero(@PathVariable Long id) {
        scheduleService.retirarDelTablero(id);
    }

    @PostMapping("/{id}/schedule/include")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCHEDULING.EDIT')")
    public void incorporarAlTablero(@PathVariable Long id,
            @Valid @RequestBody ScheduleIncludeRequest request) {
        scheduleService.incorporarAlTablero(id, request.scheduledDate(), request.reason());
    }

    @GetMapping("/{id}/schedule-history")
    @PreAuthorize("hasAuthority('SCHEDULING.VIEW')")
    public List<com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleHistoryResponse> historialProgramacion(@PathVariable Long id){return scheduleHistoryService.listar(id);}

    @PatchMapping("/{id}/vehicle")
    @PreAuthorize("hasAuthority('FLEET.EDIT')")
    public InternalServiceOrderResponse asignarVehiculo(@PathVariable Long id, @RequestBody VehicleAssignmentRequest request) {
        return service.asignarVehiculo(id, request.vehicleId());
    }
    public record VehicleAssignmentRequest(Long vehicleId) {}

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.EDIT')")
    public InternalServiceOrderResponse cambiarEstado(@PathVariable Long id, @RequestBody StatusChangeRequest request) {
        if (request.statusId() == null) throw new org.springframework.web.server.ResponseStatusException(
            HttpStatus.BAD_REQUEST, "statusId es obligatorio");
        return service.cambiarEstado(id, request.statusId(), request.reason());
    }

    public record StatusChangeRequest(Long statusId, String reason) {}

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('INTERNAL_ORDERS.EDIT')")
    public InternalServiceOrderResponse cancelar(@PathVariable Long id){return service.cancelar(id);}
}
