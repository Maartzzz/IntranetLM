package com.lmoving.intranet.audit;


import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {
    private final AuditLogService service;
    public AuditLogController(AuditLogService service){this.service=service;}
    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT.VIEW')")
    public List<AuditLogResponse> listar(@RequestParam(required=false) String module,
            @RequestParam(required=false) String entityType,
            @RequestParam(required=false) Long entityId,
            @RequestParam(required=false) String entityKey) {
        return service.listar(module,entityType,entityId,entityKey);
    }
}
