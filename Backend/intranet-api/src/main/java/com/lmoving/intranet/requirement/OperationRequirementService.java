package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.activity.OperationActivityService;
import com.lmoving.intranet.operation.*;
import com.lmoving.intranet.oscliente.*;
import com.lmoving.intranet.osinterna.*;
import com.lmoving.intranet.status.*;
import com.lmoving.intranet.requirement.dto.*;

@Service
@Transactional
public class OperationRequirementService {
    private static final String PROCESS = "REQUISITO";
    private static final String INITIAL_STATUS = "Pendiente";

    private final OperationRepository operationRepository;
    private final OperationRequirementRepository requirementRepository;
    private final RequirementCatalogRepository catalogRepository;
    private final StatusRepository statusRepository;
    private final OperationActivityService activityService;
    private final ClientServiceOrderRepository clientOrderRepository;
    private final InternalServiceOrderRepository internalOrderRepository;

    public OperationRequirementService(OperationRepository operationRepository,
            OperationRequirementRepository requirementRepository,
            RequirementCatalogRepository catalogRepository, StatusRepository statusRepository,
            OperationActivityService activityService, ClientServiceOrderRepository clientOrderRepository,
            InternalServiceOrderRepository internalOrderRepository) {
        this.operationRepository = operationRepository;
        this.requirementRepository = requirementRepository;
        this.catalogRepository = catalogRepository;
        this.statusRepository = statusRepository;
        this.activityService = activityService;
        this.clientOrderRepository = clientOrderRepository;
        this.internalOrderRepository = internalOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<OperationRequirementResponse> listar(Long operationId) {
        ensureOperation(operationId);
        return requirementRepository.findAllByOperation_IdOrderByRegisteredAtAsc(operationId)
            .stream().map(this::response).toList();
    }

    public OperationRequirementResponse crear(Long operationId, OperationRequirementRequest request) {
        Operation operation = ensureOperation(operationId);
        boolean fromCatalog = request.requirementCatalogId() != null;
        String custom = clean(request.customName());
        if (fromCatalog == (custom != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe indicar un requisito del catálogo o un nombre personalizado, pero no ambos");

        RequirementCatalog catalog = null;
        if (fromCatalog) {
            catalog = catalogRepository.findById(request.requirementCatalogId())
                .filter(RequirementCatalog::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El requisito no existe o está inactivo"));
            if (requirementRepository.existsByOperation_IdAndCatalog_Id(operationId, catalog.getId()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este requisito ya está registrado en la operación");
        }

        Status status;
        if (request.statusId() == null) {
            status = statusRepository.findFirstByProcessType_CodeAndNameAndActiveTrue(PROCESS, INITIAL_STATUS)
                .orElseThrow(() -> new IllegalStateException("No existe el estado inicial de REQUISITO en V18"));
        } else {
            status = statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), PROCESS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado no existe, está inactivo o no pertenece a REQUISITO"));
        }

        OperationRequirement entity = new OperationRequirement();
        entity.setOperation(operation);
        entity.setCatalog(catalog);
        entity.setCustomName(custom);
        entity.setExternalResponsible(clean(request.externalResponsible()));
        entity.setStatus(status);
        entity.setComment(clean(request.comment()));
        try {
            OperationRequirement saved = requirementRepository.saveAndFlush(entity);
            activityService.registrar(operationId, "CREATED", "REQUISITO", saved.getId(),
                "Se registró el requisito: " + requirementName(saved), null, requirementSnapshot(saved));
            return response(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El requisito ya está registrado en esta operación", ex);
        }
    }

    public OperationRequirementResponse actualizar(Long operationId, Long requirementId,
            OperationRequirementRequest request) {
        ensureOperation(operationId);
        OperationRequirement entity = requirementRepository.findById(requirementId)
            .filter(x -> x.getOperation().getId().equals(operationId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito de operación no encontrado"));

        boolean fromCatalog = request.requirementCatalogId() != null;
        String custom = clean(request.customName());
        if (fromCatalog == (custom != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe indicar un requisito del catálogo o un nombre personalizado, pero no ambos");

        RequirementCatalog catalog = null;
        if (fromCatalog) {
            catalog = catalogRepository.findById(request.requirementCatalogId())
                .filter(RequirementCatalog::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El requisito no existe o está inactivo"));
        }
        String oldValue = requirementSnapshot(entity);
        String oldStatus = entity.getStatus() != null ? entity.getStatus().getName() : null;
        Status status = statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), PROCESS)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe indicar un estado activo del proceso REQUISITO"));

        entity.setCatalog(catalog);
        entity.setCustomName(custom);
        entity.setExternalResponsible(clean(request.externalResponsible()));
        entity.setStatus(status);
        entity.setComment(clean(request.comment()));
        try {
            OperationRequirement saved = requirementRepository.saveAndFlush(entity);
            String newValue = requirementSnapshot(saved);
            if (!java.util.Objects.equals(oldValue, newValue)) {
                boolean statusChanged = !java.util.Objects.equals(oldStatus, saved.getStatus().getName());
                activityService.registrar(operationId, statusChanged ? "STATUS_CHANGED" : "UPDATED",
                    "REQUISITO", saved.getId(), statusChanged ? "Cambió el estado del requisito: " + requirementName(saved)
                        : "Se actualizó el requisito: " + requirementName(saved), oldValue, newValue);
            }
            return response(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El requisito ya está registrado en esta operación", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<OperationRequirementResponse> listarOssCliente(Long orderId) {
        ensureClientOrder(orderId);
        return requirementRepository.findAllByClientServiceOrder_IdOrderByRegisteredAtAsc(orderId)
            .stream().map(this::response).toList();
    }

    public OperationRequirementResponse crearOssCliente(Long orderId, OperationRequirementRequest request) {
        ClientServiceOrder owner = ensureClientOrder(orderId);
        OperationRequirement entity = crearBase(request);
        entity.setClientServiceOrder(owner);
        return guardarNuevo(entity, "El requisito ya está registrado en esta O/S cliente");
    }

    public OperationRequirementResponse actualizarOssCliente(Long orderId, Long requirementId,
            OperationRequirementRequest request) {
        ensureClientOrder(orderId);
        OperationRequirement entity = requirementRepository.findById(requirementId)
            .filter(x -> x.getClientServiceOrder() != null && x.getClientServiceOrder().getId().equals(orderId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito de O/S cliente no encontrado"));
        actualizarBase(entity, request);
        try { return response(requirementRepository.saveAndFlush(entity)); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El requisito ya está registrado en esta O/S cliente", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<OperationRequirementResponse> listarOssInterna(Long orderId) {
        ensureInternalOrder(orderId);
        return requirementRepository.findAllByInternalServiceOrder_IdOrderByRegisteredAtAsc(orderId)
            .stream().map(this::response).toList();
    }

    public OperationRequirementResponse crearOssInterna(Long orderId, OperationRequirementRequest request) {
        InternalServiceOrder owner = ensureInternalOrder(orderId);
        OperationRequirement entity = crearBase(request);
        entity.setInternalServiceOrder(owner);
        return guardarNuevo(entity, "El requisito ya está registrado en esta O/S interna");
    }

    public OperationRequirementResponse actualizarOssInterna(Long orderId, Long requirementId,
            OperationRequirementRequest request) {
        ensureInternalOrder(orderId);
        OperationRequirement entity = requirementRepository.findById(requirementId)
            .filter(x -> x.getInternalServiceOrder() != null && x.getInternalServiceOrder().getId().equals(orderId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito de O/S interna no encontrado"));
        actualizarBase(entity, request);
        try { return response(requirementRepository.saveAndFlush(entity)); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El requisito ya está registrado en esta O/S interna", ex);
        }
    }

    private OperationRequirement crearBase(OperationRequirementRequest request) {
        boolean fromCatalog = request.requirementCatalogId() != null;
        String custom = clean(request.customName());
        if (fromCatalog == (custom != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe indicar un requisito del catálogo o un nombre personalizado, pero no ambos");
        RequirementCatalog catalog = null;
        if (fromCatalog) {
            catalog = catalogRepository.findById(request.requirementCatalogId())
                .filter(RequirementCatalog::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El requisito no existe o está inactivo"));
        }
        Status status = request.statusId() == null
            ? statusRepository.findFirstByProcessType_CodeAndNameAndActiveTrue(PROCESS, INITIAL_STATUS)
                .orElseThrow(() -> new IllegalStateException("No existe el estado inicial de REQUISITO"))
            : statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), PROCESS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado no existe, está inactivo o no pertenece a REQUISITO"));
        OperationRequirement entity = new OperationRequirement();
        entity.setCatalog(catalog); entity.setCustomName(custom);
        entity.setExternalResponsible(clean(request.externalResponsible()));
        entity.setStatus(status); entity.setComment(clean(request.comment()));
        return entity;
    }

    private void actualizarBase(OperationRequirement entity, OperationRequirementRequest request) {
        boolean fromCatalog = request.requirementCatalogId() != null;
        String custom = clean(request.customName());
        if (fromCatalog == (custom != null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe indicar un requisito del catálogo o un nombre personalizado, pero no ambos");
        RequirementCatalog catalog = null;
        if (fromCatalog) catalog = catalogRepository.findById(request.requirementCatalogId())
            .filter(RequirementCatalog::isActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El requisito no existe o está inactivo"));
        Status status = statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), PROCESS)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe indicar un estado activo del proceso REQUISITO"));
        entity.setCatalog(catalog); entity.setCustomName(custom);
        entity.setExternalResponsible(clean(request.externalResponsible()));
        entity.setStatus(status); entity.setComment(clean(request.comment()));
    }

    private OperationRequirementResponse guardarNuevo(OperationRequirement entity, String conflictMessage) {
        try { return response(requirementRepository.saveAndFlush(entity)); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMessage, ex);
        }
    }

    private ClientServiceOrder ensureClientOrder(Long id) {
        return clientOrderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "O/S cliente no encontrada"));
    }
    private InternalServiceOrder ensureInternalOrder(Long id) {
        return internalOrderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "O/S interna no encontrada"));
    }

    private Operation ensureOperation(Long id) {
        return operationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada"));
    }

    private OperationRequirementResponse response(OperationRequirement x) {
        String name = x.getCatalog() != null ? x.getCatalog().getName() : x.getCustomName();
        return new OperationRequirementResponse(x.getId(),
            x.getOperation() != null ? x.getOperation().getId() : null,
            x.getClientServiceOrder() != null ? x.getClientServiceOrder().getId() : null,
            x.getInternalServiceOrder() != null ? x.getInternalServiceOrder().getId() : null,
            x.getCatalog() != null ? x.getCatalog().getId() : null, name,
            x.getExternalResponsible(), x.getStatus().getId(), x.getStatus().getName(),
            x.getStatus().getColor(), x.getComment(), x.getRegisteredAt(), x.getUpdatedAt());
    }
    private String requirementName(OperationRequirement x) {
        return x.getCatalog() != null ? x.getCatalog().getName() : x.getCustomName();
    }
    private String requirementSnapshot(OperationRequirement x) {
        return "Requisito=" + requirementName(x) + "; Responsable=" + x.getExternalResponsible()
            + "; Estado=" + (x.getStatus() != null ? x.getStatus().getName() : null)
            + "; Comentario=" + x.getComment();
    }
    private String clean(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
