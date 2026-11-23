package com.lmoving.intranet.oscliente;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.lmoving.intranet.audit.AuditLogService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.cargotype.CargoType;
import com.lmoving.intranet.cargotype.repository.CargoTypeRepository;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.repository.ClientRepository;
import com.lmoving.intranet.operation.Operation;
import com.lmoving.intranet.operation.OperationRepository;
import com.lmoving.intranet.status.Status;
import com.lmoving.intranet.status.StatusRepository;

@Service
@Transactional
public class ClientServiceOrderService {
    private static final String PROCESS = "OS_CLIENTE";
    @Autowired private AuditLogService auditLogService;
    private final ClientServiceOrderRepository orderRepository;
    private final ClientServiceOrderFclQuantityRepository fclRepository;
    private final ClientServiceOrderLclQuantityRepository lclRepository;
    private final ClientRepository clientRepository;
    private final OperationRepository operationRepository;
    private final CargoTypeRepository cargoTypeRepository;
    private final StatusRepository statusRepository;
    private final com.lmoving.intranet.osinterna.InternalServiceOrderRepository internalOrderRepository;
    private final com.lmoving.intranet.requirement.OperationRequirementRepository requirementRepository;
    private final com.lmoving.intranet.osinterna.InternalServiceOrderFclRepository internalFclRepository;
    private final com.lmoving.intranet.osinterna.InternalServiceOrderLclRepository internalLclRepository;

    public ClientServiceOrderService(ClientServiceOrderRepository orderRepository,
            ClientServiceOrderFclQuantityRepository fclRepository,
            ClientServiceOrderLclQuantityRepository lclRepository,
            ClientRepository clientRepository, OperationRepository operationRepository,
            CargoTypeRepository cargoTypeRepository, StatusRepository statusRepository,
            com.lmoving.intranet.osinterna.InternalServiceOrderFclRepository internalFclRepository,
            com.lmoving.intranet.osinterna.InternalServiceOrderLclRepository internalLclRepository,
            com.lmoving.intranet.osinterna.InternalServiceOrderRepository internalOrderRepository,
            com.lmoving.intranet.requirement.OperationRequirementRepository requirementRepository) {
        this.orderRepository = orderRepository; this.fclRepository = fclRepository;
        this.lclRepository = lclRepository; this.clientRepository = clientRepository;
        this.operationRepository = operationRepository; this.cargoTypeRepository = cargoTypeRepository;
        this.statusRepository = statusRepository;
        this.internalOrderRepository = internalOrderRepository;
        this.requirementRepository = requirementRepository;
        this.internalFclRepository = internalFclRepository;
        this.internalLclRepository = internalLclRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientServiceOrderResponse> listar(Long clientId) {
        List<ClientServiceOrder> orders = clientId == null
            ? orderRepository.findAllByOrderByCreatedAtDesc()
            : orderRepository.findAllByClient_IdOrderByCreatedAtDesc(clientId);
        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientServiceOrderResponse obtener(Long id) { return toResponse(buscar(id)); }

    public ClientServiceOrderResponse crear(ClientServiceOrderRequest request) {
        String number = request.clientOrderNumber().trim();
        if (orderRepository.existsByClient_IdAndClientOrderNumberIgnoreCase(request.clientId(), number))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El cliente ya tiene registrada una O/S con el número " + number);

        Client client = clientRepository.findById(request.clientId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        if (!client.isActive()) throw new IllegalArgumentException("El cliente está inactivo");

        Operation operation = null;
        if (request.operationId() != null) {
            operation = operationRepository.findById(request.operationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada"));
            validarOperacionParaNuevaOs(operation);
            if (orderRepository.existsByOperation_Id(operation.getId()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La operación ya tiene una O/S del cliente asociada");
            if (!operation.getClient().getId().equals(client.getId()))
                throw new IllegalArgumentException("La operación seleccionada pertenece a otro cliente");
            boolean operationIsFcl = operation.getCargoType().isContainer();
            if (operationIsFcl != request.serviceType().equals("FCL"))
                throw new IllegalArgumentException("El tipo de servicio debe coincidir con el de la operación");
        }

        validarCantidades(request);
        Status status = request.statusId() == null
            ? statusRepository.findFirstByProcessType_CodeAndNameAndActiveTrue(PROCESS, "Recibida")
                .orElseThrow(() -> new IllegalStateException("No existe el estado inicial Recibida para OS_CLIENTE"))
            : statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), PROCESS)
                .orElseThrow(() -> new IllegalArgumentException("El estado no existe, está inactivo o no pertenece a OS_CLIENTE"));

        ClientServiceOrder order = new ClientServiceOrder();
        order.setClientOrderNumber(number);
        order.setClient(client);
        order.setOperation(operation);
        order.setReference(normalizar(request.reference()));
        order.setServiceType(request.serviceType());
        order.setReceivedDate(request.receivedDate());
        order.setEstimatedArrivalDate(request.estimatedArrivalDate());
        order.setOrigin(normalizar(request.origin()));
        order.setDestination(normalizar(request.destination()));
        order.setObservation(normalizar(request.observation()));
        order.setStatus(status);
        ClientServiceOrder saved = orderRepository.saveAndFlush(order);

        if ("FCL".equals(request.serviceType())) {
            for (ClientServiceOrderRequest.FclQuantityRequest item : request.fclQuantities()) {
                CargoType cargoType = cargoTypeRepository.findById(item.cargoTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de contenedor no encontrado: " + item.cargoTypeId()));
                if (!cargoType.isActive() || !cargoType.isContainer())
                    throw new IllegalArgumentException("El tipo seleccionado no es un contenedor activo");
                fclRepository.save(new ClientServiceOrderFclQuantity(saved, cargoType, item.totalQuantity()));
            }
        } else {
            lclRepository.save(new ClientServiceOrderLclQuantity(saved, request.totalPackages()));
        }
        ClientServiceOrderResponse response = toResponse(saved);
        auditLogService.registrar("OS_CLIENTE", "OS_CLIENTE", saved.getId(), "CREATED",
            "Se creó la O/S del cliente " + saved.getClientOrderNumber(), null, response.toString());
        return response;
    }

    public ClientServiceOrderResponse actualizar(Long id, ClientServiceOrderRequest request) {
        ClientServiceOrder order = orderRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "O/S del cliente no encontrada"));
        String oldSnapshot = toResponse(order).toString();
        String number = request.clientOrderNumber().trim();
        if (orderRepository.existsByClient_IdAndClientOrderNumberIgnoreCaseAndIdNot(request.clientId(), number, id))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El cliente ya tiene registrada una O/S con el número " + number);

        Client client = clientRepository.findById(request.clientId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        if (!client.isActive()) throw new IllegalArgumentException("El cliente está inactivo");

        if (!order.getServiceType().equals(request.serviceType()))
            throw new IllegalArgumentException("No se puede cambiar el tipo de servicio de una O/S del cliente existente");

        Operation operation = null;
        if (request.operationId() != null) {
            operation = operationRepository.findById(request.operationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada"));
            boolean sameOperation = order.getOperation() != null
                && order.getOperation().getId().equals(operation.getId());
            if (!sameOperation) {
                validarOperacionParaNuevaOs(operation);
                if (orderRepository.existsByOperation_IdAndIdNot(operation.getId(), id))
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La operación ya tiene otra O/S del cliente asociada");
            }
            if (!operation.getClient().getId().equals(client.getId()))
                throw new IllegalArgumentException("La operación seleccionada pertenece a otro cliente");
            boolean operationIsFcl = operation.getCargoType().isContainer();
            if (operationIsFcl != request.serviceType().equals("FCL"))
                throw new IllegalArgumentException("El tipo de servicio debe coincidir con el de la operación");
        }

        validarCantidades(request);
        Status status = request.statusId() == null ? order.getStatus()
            : statusRepository.findByIdAndProcessType_CodeAndActiveTrue(request.statusId(), PROCESS)
                .orElseThrow(() -> new IllegalArgumentException("El estado no existe, está inactivo o no pertenece a OS_CLIENTE"));

        if ("FCL".equals(request.serviceType())) actualizarCantidadesFcl(order, request);
        else actualizarCantidadLcl(order, request);

        order.setClientOrderNumber(number);
        order.setClient(client);
        order.setOperation(operation);
        order.setReference(normalizar(request.reference()));
        order.setReceivedDate(request.receivedDate());
        order.setEstimatedArrivalDate(request.estimatedArrivalDate());
        order.setOrigin(normalizar(request.origin()));
        order.setDestination(normalizar(request.destination()));
        order.setObservation(normalizar(request.observation()));
        order.setStatus(status);
        ClientServiceOrder saved = orderRepository.saveAndFlush(order);
        ClientServiceOrderResponse response = toResponse(saved);
        auditLogService.registrar("OS_CLIENTE", "OS_CLIENTE", saved.getId(), "UPDATED",
            "Se actualizaron los datos de la O/S del cliente " + saved.getClientOrderNumber(),
            oldSnapshot, response.toString());
        return response;
    }

    private void validarOperacionParaNuevaOs(Operation operation) {
        if (operation.getStatus() == null || operation.getStatus().getName() == null
                || !"Confirmada".equalsIgnoreCase(operation.getStatus().getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Solo se puede registrar una O/S del cliente para una operación en estado Confirmada");
        }
    }

    private void actualizarCantidadesFcl(ClientServiceOrder order, ClientServiceOrderRequest request) {
        var current = fclRepository.findAllByClientServiceOrder_IdOrderByCargoType_NameAsc(order.getId());
        var requestedIds = request.fclQuantities().stream()
            .map(ClientServiceOrderRequest.FclQuantityRequest::cargoTypeId).collect(java.util.stream.Collectors.toSet());
        for (var existing : current) {
            Long typeId = existing.getCargoType().getId();
            long distributed = internalFclRepository.countDistributedByCargoType(order.getId(), typeId, null);
            var replacement = request.fclQuantities().stream().filter(q -> q.cargoTypeId().equals(typeId)).findFirst();
            if (replacement.isEmpty() && distributed > 0)
                throw new IllegalArgumentException("No se puede retirar el tipo de contenedor " + existing.getCargoType().getName() + " porque ya tiene O/S internas distribuidas");
            if (replacement.isPresent() && replacement.get().totalQuantity() < distributed)
                throw new IllegalArgumentException("La cantidad de " + existing.getCargoType().getName() + " no puede ser menor a las " + distributed + " unidades ya distribuidas");
            if (replacement.isPresent()) existing.setTotalQuantity(replacement.get().totalQuantity());
        }
        for (var item : request.fclQuantities()) {
            if (requestedIds.contains(item.cargoTypeId()) && current.stream().noneMatch(q -> q.getCargoType().getId().equals(item.cargoTypeId()))) {
                CargoType cargoType = cargoTypeRepository.findById(item.cargoTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de contenedor no encontrado: " + item.cargoTypeId()));
                if (!cargoType.isActive() || !cargoType.isContainer())
                    throw new IllegalArgumentException("El tipo seleccionado no es un contenedor activo");
                fclRepository.save(new ClientServiceOrderFclQuantity(order, cargoType, item.totalQuantity()));
            }
        }
        fclRepository.flush();
        for (var existing : current) {
            boolean retained = requestedIds.contains(existing.getCargoType().getId());
            if (!retained) fclRepository.delete(existing);
        }
        lclRepository.deleteById(order.getId());
    }

    private void actualizarCantidadLcl(ClientServiceOrder order, ClientServiceOrderRequest request) {
        long distributed = internalLclRepository.sumDistributedPackages(order.getId(), null);
        if (request.totalPackages() < distributed)
            throw new IllegalArgumentException("La cantidad de bultos no puede ser menor a los " + distributed + " ya distribuidos en O/S internas");
        fclRepository.deleteAllByClientServiceOrder_Id(order.getId());
        var quantity = lclRepository.findById(order.getId()).orElse(null);
        if (quantity == null) lclRepository.save(new ClientServiceOrderLclQuantity(order, request.totalPackages()));
        else quantity.setTotalPackages(request.totalPackages());
    }

    private void validarCantidades(ClientServiceOrderRequest request) {
        if ("FCL".equals(request.serviceType())) {
            if (request.fclQuantities() == null || request.fclQuantities().isEmpty())
                throw new IllegalArgumentException("Debe registrar al menos una cantidad por tipo de contenedor");
            if (request.totalPackages() != null)
                throw new IllegalArgumentException("Una O/S FCL no debe registrar bultos LCL");
            long distinct = request.fclQuantities().stream().map(ClientServiceOrderRequest.FclQuantityRequest::cargoTypeId).distinct().count();
            if (distinct != request.fclQuantities().size())
                throw new IllegalArgumentException("No puede repetir un tipo de contenedor en la misma O/S");
        } else {
            if (request.totalPackages() == null || request.totalPackages() < 0)
                throw new IllegalArgumentException("Debe indicar el total de bultos de la O/S LCL");
            if (request.fclQuantities() != null && !request.fclQuantities().isEmpty())
                throw new IllegalArgumentException("Una O/S LCL no debe registrar cantidades de contenedores");
        }
    }

    public ClientServiceOrderResponse cambiarActivo(Long id, boolean active) {
        ClientServiceOrder order = buscar(id);
        boolean previous = order.isActive();
        order.setActive(active);
        ClientServiceOrder saved = orderRepository.saveAndFlush(order);
        auditLogService.registrar("OS_CLIENTE", "OS_CLIENTE", saved.getId(), active ? "ACTIVATED" : "DEACTIVATED",
            active ? "Se activó la O/S del cliente" : "Se desactivó la O/S del cliente",
            Boolean.toString(previous), Boolean.toString(active));
        return toResponse(saved);
    }

    public void eliminar(Long id) {
        ClientServiceOrder order = buscar(id);
        if (internalOrderRepository.existsByClientServiceOrder_Id(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: existen O/S internas asociadas. Desactive la O/S del cliente.");
        if (requirementRepository.existsByClientServiceOrder_Id(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: la O/S tiene requisitos asociados. Desactive la O/S para conservar su historial.");
        auditLogService.registrar("OS_CLIENTE", "OS_CLIENTE", id, "DELETED",
            "Se eliminó la O/S del cliente " + order.getClientOrderNumber(), toResponse(order).toString(), null);
        orderRepository.delete(order);
        orderRepository.flush();
    }

    private ClientServiceOrder buscar(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "O/S del cliente no encontrada"));
    }

    private ClientServiceOrderResponse toResponse(ClientServiceOrder order) {
        List<ClientServiceOrderResponse.FclQuantityResponse> fcl = "FCL".equals(order.getServiceType())
            ? fclRepository.findAllByClientServiceOrder_IdOrderByCargoType_NameAsc(order.getId()).stream()
                .map(q -> new ClientServiceOrderResponse.FclQuantityResponse(q.getCargoType().getId(), q.getCargoType().getName(), q.getTotalQuantity())).toList()
            : List.of();
        Integer packages = "LCL".equals(order.getServiceType())
            ? lclRepository.findById(order.getId()).map(ClientServiceOrderLclQuantity::getTotalPackages).orElse(null) : null;
        return new ClientServiceOrderResponse(order.getId(), order.getClientOrderNumber(),
            order.getClient().getId(), order.getClient().getBusinessName(),
            order.getOperation() != null ? order.getOperation().getId() : null,
            order.getOperation() != null ? order.getOperation().getOperationNumber() : null,
            order.getReference(), order.getServiceType(), order.getReceivedDate(), order.getEstimatedArrivalDate(),
            order.getOrigin(), order.getDestination(), order.getObservation(), order.getStatus().getId(),
            order.getStatus().getName(), order.getStatus().getColor(), fcl, packages,
            order.getCreatedAt(), order.getUpdatedAt(), order.isActive());
    }

    private String normalizar(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
