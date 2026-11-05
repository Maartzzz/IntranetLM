package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.requirement.dto.*;

@Service
@Transactional
public class RequirementCatalogService {
    private final RequirementCatalogRepository repository;

    public RequirementCatalogService(RequirementCatalogRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RequirementCatalogResponse> listar(boolean soloActivos) {
        return (soloActivos ? repository.findAllByActiveTrueOrderByNameAsc()
                : repository.findAllByOrderByNameAsc()).stream().map(this::response).toList();
    }

    public RequirementCatalogResponse crear(RequirementCatalogRequest request) {
        String name = clean(request.name());
        if (repository.existsByNameIgnoreCase(name))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un requisito con ese nombre");
        RequirementCatalog entity = new RequirementCatalog();
        entity.setName(name);
        entity.setDescription(clean(request.description()));
        entity.setActive(request.active() == null || request.active());
        return response(repository.save(entity));
    }

    public RequirementCatalogResponse actualizar(Long id, RequirementCatalogRequest request) {
        RequirementCatalog entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito de catálogo no encontrado"));
        String name = clean(request.name());
        boolean duplicated = repository.findAll().stream()
            .anyMatch(x -> !x.getId().equals(id) && x.getName().equalsIgnoreCase(name));
        if (duplicated) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un requisito con ese nombre");
        entity.setName(name);
        entity.setDescription(clean(request.description()));
        if (request.active() != null) entity.setActive(request.active());
        return response(repository.save(entity));
    }

    private RequirementCatalogResponse response(RequirementCatalog x) {
        return new RequirementCatalogResponse(x.getId(), x.getName(), x.getDescription(),
            x.isActive(), x.getCreatedAt(), x.getUpdatedAt());
    }
    private String clean(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
