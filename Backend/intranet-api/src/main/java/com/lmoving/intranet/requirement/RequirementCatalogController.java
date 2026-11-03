package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lmoving.intranet.requirement.dto.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/requirement-catalog")
public class RequirementCatalogController {
    private final RequirementCatalogService service;
    public RequirementCatalogController(RequirementCatalogService service) { this.service = service; }

    @GetMapping
    public List<RequirementCatalogResponse> listar(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.listar(activeOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public RequirementCatalogResponse crear(@Valid @RequestBody RequirementCatalogRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public RequirementCatalogResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody RequirementCatalogRequest request) {
        return service.actualizar(id, request);
    }
}
