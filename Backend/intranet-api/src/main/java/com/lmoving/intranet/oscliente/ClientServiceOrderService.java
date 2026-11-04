package com.lmoving.intranet.oscliente;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
    private final ClientServiceOrderRepository orderRepository;
    private final ClientServiceOrderFclQuantityRepository fclRepository;
    private final ClientServiceOrderLclQuantityRepository lclRepository;
    private final ClientRepository clientRepository;
    private final OperationRepository operationRepository;
    private final CargoTypeRepository cargoTypeRepository;
    private final StatusRepository statusRepository;

    public ClientServiceOrderService(ClientServiceOrderRepository orderRepository,
            ClientServiceOrderFclQuantityRepository fclRepository,
            ClientServiceOrderLclQuantityRepository lclRepository,
            ClientRepository clientRepository, OperationRepository operationRepository,
            CargoTypeRepository cargoTypeRepository, StatusRepository statusRepository) {
        this.orderRepository = orderRepository; this.fclRepository = fclRepository;
        this.lclRepository = lclRepository; this.clientRepository = clientRepository;
        this.operationRepository = operationRepository; this.cargoTypeRepository = cargoTypeRepository;
        this.statusRepository = statusRepository;
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
        return toResponse(saved);
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
            order.getCreatedAt(), order.getUpdatedAt());
    }

    private String normalizar(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
