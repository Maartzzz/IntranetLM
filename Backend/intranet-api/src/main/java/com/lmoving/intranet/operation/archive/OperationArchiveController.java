package com.lmoving.intranet.operation.archive;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations/archive")
public class OperationArchiveController {
    private final OperationArchiveService service;
    public OperationArchiveController(OperationArchiveService service) { this.service = service; }

    @GetMapping
    public List<OperationArchiveResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return service.list(status, search);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public OperationArchiveResponse restore(@PathVariable Long id) {
        return service.restore(id);
    }
}
