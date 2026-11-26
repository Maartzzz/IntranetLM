package com.lmoving.intranet.operation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.activity.OperationActivityService;
import com.lmoving.intranet.audit.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.cargotype.repository.CargoTypeRepository;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.ClientSubclient;
import com.lmoving.intranet.client.repository.ClientSubclientRepository;
import com.lmoving.intranet.client.repository.ClientRepository;
import com.lmoving.intranet.status.Status;
import com.lmoving.intranet.status.StatusRepository;

@Service
@Transactional
public class OperationService {
    private final OperationRepository operationRepository;
    private final ClientRepository clientRepository;
    private final CargoTypeRepository cargoTypeRepository;
    private final ClientSubclientRepository clientSubclientRepository;
    private final StatusRepository statusRepository;
    private final OperationActivityService activityService;
    @Autowired private AuditLogService auditLogService;

    public OperationService(OperationRepository operationRepository, ClientRepository clientRepository,
            CargoTypeRepository cargoTypeRepository, StatusRepository statusRepository,
            OperationActivityService activityService, ClientSubclientRepository clientSubclientRepository) {
        this.operationRepository = operationRepository;
        this.clientRepository = clientRepository;
        this.cargoTypeRepository = cargoTypeRepository;
        this.clientSubclientRepository = clientSubclientRepository;
        this.statusRepository = statusRepository;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> listarTodas() {
        return operationRepository.findAllByArchivedFalseOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> listarPorCliente(Long clientId) {
        if (!clientRepository.existsById(clientId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        return operationRepository.findAllByClient_IdAndArchivedFalseOrderByCreatedAtDesc(clientId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OperationResponse obtener(Long id) { return toResponse(buscar(id)); }

    public OperationResponse crear(OperationRequest request) {
        Operation operation = new Operation();
        Long sequence = operationRepository.nextOperationNumber();
        operation.setOperationNumber("OP-" + String.format("%06d", sequence));
        aplicarDatos(operation, request);
        if (request.statusId() == null) {
            Status initial = statusRepository.findFirstByProcessType_CodeAndNameAndActiveTrue("OPERACION", "Registrada")
                .orElseThrow(() -> new IllegalStateException("No existe estado inicial para OPERACION"));
            operation.setStatus(initial);
        }
        Operation saved = operationRepository.saveAndFlush(operation);
        String currentSnapshot = snapshot(saved);
        activityService.registrar(saved.getId(), "CREATED", "OPERACION", saved.getId(),
            "Se creó la operación " + saved.getOperationNumber(), null, currentSnapshot);
        auditLogService.registrar("OPERACIONES", "OPERACION", saved.getId(), "CREATED",
            "Se creó la operación " + saved.getOperationNumber(), null, currentSnapshot);
        return toResponse(saved);
    }

    public OperationResponse actualizar(Long id, OperationRequest request) {
        Operation operation = buscar(id);
        if (operation.isArchived()) throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar una operación archivada; primero debe restaurarla");
        String oldValue = snapshot(operation);
        String oldStatus = operation.getStatus() != null ? operation.getStatus().getName() : null;
        aplicarDatos(operation, request);
        if (!java.util.Objects.equals(oldStatus, operation.getStatus() != null ? operation.getStatus().getName() : null)) {
            operation.setStatusChangedAt(java.time.LocalDateTime.now());
        }
        Operation saved = operationRepository.saveAndFlush(operation);
        String newStatus = saved.getStatus() != null ? saved.getStatus().getName() : null;
        String newValue = snapshot(saved);
        if (!java.util.Objects.equals(oldValue, newValue)) {
            boolean statusChanged = !java.util.Objects.equals(oldStatus, newStatus);
            String action = statusChanged ? "STATUS_CHANGED" : "UPDATED";
            String description = statusChanged ? "Cambió el estado de la operación" : "Se actualizaron los datos de la operación";
            activityService.registrar(saved.getId(), action, "OPERACION", saved.getId(),
                description, oldValue, newValue);
            auditLogService.registrar("OPERACIONES", "OPERACION", saved.getId(), action,
                description, oldValue, newValue);
        }
        return toResponse(saved);
    }

    private Operation buscar(Long id) {
        return operationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada"));
    }

    private void aplicarDatos(Operation operation, OperationRequest request) {
        Client client = clientRepository.findById(request.clientId())
            .orElseThrow(() -> new IllegalArgumentException("El cliente indicado no existe"));
        if (!client.isActive()) throw new IllegalArgumentException("El cliente indicado está inactivo");

        CargoType cargo = cargoTypeRepository.findById(request.cargoTypeId())
            .orElseThrow(() -> new IllegalArgumentException("El tipo de carga indicado no existe"));
        if (!cargo.isActive()) throw new IllegalArgumentException("El tipo de carga indicado está inactivo");

        operation.setReference(normalizar(request.reference()));
        operation.setClient(client);

        ClientSubclient clientSubclient = null;
        if (request.clientSubclientId() != null) {
            clientSubclient = clientSubclientRepository.findById(request.clientSubclientId())
                .orElseThrow(() -> new IllegalArgumentException("La relación entre clientes indicada no existe"));
            if (!clientSubclient.isActive()) {
                throw new IllegalArgumentException("La relación entre clientes está inactiva");
            }
            if (!clientSubclient.getClient().getId().equals(client.getId())
                    && !clientSubclient.getSubclient().getId().equals(client.getId())) {
                throw new IllegalArgumentException("La relación entre clientes no corresponde al cliente seleccionado");
            }
        }
        operation.setClientSubclient(clientSubclient);
        operation.setCargoType(cargo);
        operation.setCustomerOrderNumber(normalizar(request.customerOrderNumber()));
        operation.setOrigin(normalizar(request.origin()));
        operation.setDestination(normalizar(request.destination()));
        operation.setQuantity(request.quantity());
        operation.setEstimatedDate(request.estimatedDate());
        operation.setObservation(normalizar(request.observation()));

        if (request.statusId() != null) {
            Status status = statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), "OPERACION")
                .orElseThrow(() -> new IllegalArgumentException("El estado no existe, está inactivo o no pertenece a OPERACION"));
            operation.setStatus(status);
        }
    }

    private String snapshot(Operation o) {
        return "Referencia=" + o.getReference() + "; Cliente=" + (o.getClient() != null ? o.getClient().getBusinessName() : null)
            + "; Subcliente=" + (o.getClientSubclient() != null ? o.getClientSubclient().getSubclient().getBusinessName() : null)
            + "; TipoCarga=" + (o.getCargoType() != null ? o.getCargoType().getName() : null)
            + "; Pedido=" + o.getCustomerOrderNumber() + "; Origen=" + o.getOrigin()
            + "; Destino=" + o.getDestination() + "; Cantidad=" + o.getQuantity()
            + "; Fecha=" + o.getEstimatedDate() + "; Estado=" + (o.getStatus() != null ? o.getStatus().getName() : null)
            + "; Observación=" + o.getObservation();
    }

    private String normalizar(String v) { return v == null || v.isBlank() ? null : v.trim(); }

    private OperationResponse toResponse(Operation o) {
        return new OperationResponse(o.getId(), o.getOperationNumber(), o.getReference(),
            o.getClient().getId(), o.getClient().getBusinessName(),
            o.getClientSubclient() != null ? o.getClientSubclient().getId() : null,
            o.getClientSubclient() != null ? o.getClientSubclient().getSubclient().getId() : null,
            o.getClientSubclient() != null ? o.getClientSubclient().getSubclient().getBusinessName() : null,
            o.getCargoType().getId(), o.getCargoType().getName(), o.getCargoType().isContainer(),
            o.getCustomerOrderNumber(), o.getOrigin(), o.getDestination(), o.getQuantity(), o.getEstimatedDate(),
            o.getStatus() != null ? o.getStatus().getId() : null,
            o.getStatus() != null ? o.getStatus().getName() : null,
            o.getStatus() != null ? o.getStatus().getColor() : null,
            o.getObservation(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
