
package com.lmoving.intranet.status;

import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statuses")
public class StatusController {

    private final StatusRepository statusRepository;

    public StatusController(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @GetMapping
    public List<StatusResponse> listarPorProceso(
            @RequestParam String processCode) {

        String code = processCode.trim().toUpperCase(Locale.ROOT);

        return statusRepository
                .findAllByProcessType_CodeAndActiveTrueOrderByIdAsc(code)
                .stream()
                .map(status -> new StatusResponse(
                        status.getId(),
                        status.getName(),
                        status.getDescription(),
                        status.getColor(),
                        status.isActive()
                ))
                .toList();
    }

    public record StatusResponse(
            Long id,
            String name,
            String description,
            String color,
            boolean active
    ) {}
}
