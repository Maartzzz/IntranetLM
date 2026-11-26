package com.lmoving.intranet.status;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/statuses")
public class StatusController {
    private final StatusRepository statusRepository;
    private final StatusProcessTypeRepository processRepository;

    public StatusController(StatusRepository statusRepository, StatusProcessTypeRepository processRepository) {
        this.statusRepository = statusRepository;
        this.processRepository = processRepository;
    }

    @GetMapping
    public List<StatusResponse> listarPorProceso(@RequestParam String processCode) {
        String code = processCode.trim().toUpperCase(Locale.ROOT);
        return statusRepository.findAllByProcessType_CodeAndActiveTrueOrderByIdAsc(code)
            .stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse crear(@RequestBody StatusRequest request) {
        String code = normalize(request.processCode());
        StatusProcessType process = processRepository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proceso no encontrado"));
        if (request.name() == null || request.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del estado es obligatorio");
        if (statusRepository.findFirstByProcessType_CodeAndNameAndActiveTrue(code, request.name().trim()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un estado activo con ese nombre");
        Status status = new Status();
        status.setProcessType(process);
        apply(status, request);
        return toResponse(statusRepository.save(status));
    }

    @PutMapping("/{id}")
    public StatusResponse actualizar(@PathVariable Long id, @RequestBody StatusRequest request) {
        Status status = statusRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado"));
        if (request.name() == null || request.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del estado es obligatorio");
        if (request.processCode() != null && !request.processCode().isBlank()
                && !status.getProcessType().getCode().equalsIgnoreCase(request.processCode().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se permite mover un estado a otro proceso");
        apply(status, request);
        return toResponse(statusRepository.save(status));
    }

    private void apply(Status status, StatusRequest request) {
        status.setName(request.name().trim());
        status.setDescription(blankToNull(request.description()));
        status.setColor(blankToNull(request.color()));
        status.setActive(request.active());
        status.setInitial(request.initial());
        status.setFinalStatus(request.finalStatus());
        status.setAutoTransition(request.autoTransition());
        status.setAllowsScheduling(request.allowsScheduling());
    }

    private StatusResponse toResponse(Status s) {
        return new StatusResponse(s.getId(), s.getProcessType().getCode(), s.getName(), s.getDescription(),
            s.getColor(), s.isActive(), s.isInitial(), s.isFinalStatus(), s.isAutoTransition(), s.isAllowsScheduling());
    }
    private String normalize(String value) {
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "processCode es obligatorio");
        return value.trim().toUpperCase(Locale.ROOT);
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record StatusRequest(String processCode, String name, String description, String color,
        boolean active, boolean initial, boolean finalStatus, boolean autoTransition, boolean allowsScheduling) {}
    public record StatusResponse(Long id, String processCode, String name, String description, String color,
        boolean active, boolean initial, boolean finalStatus, boolean autoTransition, boolean allowsScheduling) {}
}
