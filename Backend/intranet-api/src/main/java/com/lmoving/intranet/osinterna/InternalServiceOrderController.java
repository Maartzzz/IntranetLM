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
    public List<InternalServiceOrderResponse> listar(@RequestParam(required=false) Long clientId){return service.listar(clientId);}

    @GetMapping("/{id}")
    public InternalServiceOrderResponse obtener(@PathVariable Long id){return service.obtener(id);}

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public InternalServiceOrderResponse actualizar(@PathVariable Long id,@Valid @RequestBody InternalServiceOrderRequest request){return service.actualizar(id,request);}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public InternalServiceOrderResponse crear(@Valid @RequestBody InternalServiceOrderRequest request){return service.crear(request);}


    @GetMapping("/schedule")
    public InternalServiceOrderScheduleService.ScheduleBoardResponse obtenerTablero(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return scheduleService.obtenerTablero(startDate, endDate);
    }

    @PostMapping("/{id}/schedule/exclude")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public void retirarDelTablero(@PathVariable Long id) {
        scheduleService.retirarDelTablero(id);
    }

    @PostMapping("/{id}/schedule/include")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public void incorporarAlTablero(@PathVariable Long id,
            @Valid @RequestBody ScheduleIncludeRequest request) {
        scheduleService.incorporarAlTablero(id, request.scheduledDate(), request.reason());
    }

    @GetMapping("/{id}/schedule-history")
    public List<com.lmoving.intranet.osinterna.schedule.InternalServiceOrderScheduleHistoryResponse> historialProgramacion(@PathVariable Long id){return scheduleHistoryService.listar(id);}

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERACIONES')")
    public InternalServiceOrderResponse cancelar(@PathVariable Long id){return service.cancelar(id);}
}
