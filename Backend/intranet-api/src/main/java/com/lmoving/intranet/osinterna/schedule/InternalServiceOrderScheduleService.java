package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lmoving.intranet.osinterna.InternalServiceOrder;
import com.lmoving.intranet.osinterna.InternalServiceOrderFcl;
import com.lmoving.intranet.osinterna.InternalServiceOrderFclRepository;
import com.lmoving.intranet.osinterna.InternalServiceOrderLcl;
import com.lmoving.intranet.osinterna.InternalServiceOrderLclRepository;
import com.lmoving.intranet.osinterna.InternalServiceOrderRepository;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleFclDetailResponse;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleLclDetailResponse;
import com.lmoving.intranet.osinterna.schedule.dto.ScheduleOrderResponse;

@Service
@Transactional
public class InternalServiceOrderScheduleService {
    private final InternalServiceOrderRepository orderRepository;
    private final InternalServiceOrderFclRepository fclRepository;
    private final InternalServiceOrderLclRepository lclRepository;
    private final InternalServiceOrderScheduleExclusionRepository exclusionRepository;
    private final InternalServiceOrderScheduleHistoryService historyService;

    public InternalServiceOrderScheduleService(
            InternalServiceOrderRepository orderRepository,
            InternalServiceOrderFclRepository fclRepository,
            InternalServiceOrderLclRepository lclRepository,
            InternalServiceOrderScheduleExclusionRepository exclusionRepository,
            InternalServiceOrderScheduleHistoryService historyService) {
        this.orderRepository = orderRepository;
        this.fclRepository = fclRepository;
        this.lclRepository = lclRepository;
        this.exclusionRepository = exclusionRepository;
        this.historyService = historyService;
    }

    @Transactional(readOnly = true)
    public ScheduleBoardResponse obtenerTablero(LocalDate startDate, LocalDate endDate) {
        validarPeriodo(startDate, endDate);

        List<InternalServiceOrder> scheduled = orderRepository.findScheduledForPeriod(startDate, endDate);
        List<InternalServiceOrder> available = new ArrayList<>(orderRepository.findAvailableForPeriod(startDate, endDate));

        List<InternalServiceOrderScheduleExclusion> exclusions =
                exclusionRepository.findAllByExcludedDateBetween(startDate, endDate);
        Set<String> excludedKeys = new HashSet<>();
        for (InternalServiceOrderScheduleExclusion exclusion : exclusions) {
            excludedKeys.add(key(exclusion.getInternalServiceOrder().getId(), exclusion.getExcludedDate()));
        }

        List<InternalServiceOrder> visibleScheduled = new ArrayList<>();
        for (InternalServiceOrder order : scheduled) {
            if (excludedKeys.contains(key(order.getId(), order.getScheduledDate()))) {
                available.add(order);
            } else {
                visibleScheduled.add(order);
            }
        }

        available.sort(Comparator
                .comparing(InternalServiceOrder::getScheduledDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(InternalServiceOrder::getInternalOrderNumber));

        return new ScheduleBoardResponse(startDate, endDate,
                mapOrders(visibleScheduled), mapOrders(available));
    }

    public void retirarDelTablero(Long orderId) {
        InternalServiceOrder order = buscar(orderId);
        if (order.getScheduledDate() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La O/S interna no tiene fecha programada y no está dentro del tablero");
        }
        if (esCancelada(order)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede retirar del tablero una O/S cancelada");
        }
        LocalDate date = order.getScheduledDate();
        if (!exclusionRepository.existsByInternalServiceOrder_IdAndExcludedDate(orderId, date)) {
            exclusionRepository.save(new InternalServiceOrderScheduleExclusion(order, date));
        }
    }

    public void incorporarAlTablero(Long orderId, LocalDate scheduledDate, String reason) {
        if (scheduledDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de programación es obligatoria");
        }
        InternalServiceOrder order = buscar(orderId);
        if (esCancelada(order)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede incorporar una O/S cancelada al tablero");
        }
        LocalDate previousDate = order.getScheduledDate();
        order.setScheduledDate(scheduledDate);
        orderRepository.saveAndFlush(order);
        exclusionRepository.deleteAllByInternalServiceOrder_Id(orderId);

        if (!scheduledDate.equals(previousDate)) {
            String event = previousDate == null ? "PROGRAMACION_INICIAL" : "REPROGRAMACION";
            String normalizedReason = reason == null || reason.isBlank()
                    ? "Programación desde el tablero" : reason.trim();
            historyService.registrar(order, event, previousDate, scheduledDate, normalizedReason);
        }
    }

    private List<ScheduleOrderResponse> mapOrders(List<InternalServiceOrder> orders) {
        if (orders.isEmpty()) return List.of();
        List<Long> ids = orders.stream().map(InternalServiceOrder::getId).toList();
        var fclById = new java.util.HashMap<Long, InternalServiceOrderFcl>();
        for (InternalServiceOrderFcl detail : fclRepository.findAllByIdIn(ids)) {
            fclById.put(detail.getId(), detail);
        }
        var lclById = new java.util.HashMap<Long, InternalServiceOrderLcl>();
        for (InternalServiceOrderLcl detail : lclRepository.findAllByIdIn(ids)) {
            lclById.put(detail.getId(), detail);
        }

        return orders.stream().map(order -> {
            var client = order.getClient();
            var clientOrder = order.getClientServiceOrder();
            var origin = order.getOrigin();
            var destination = order.getDestination();
            var provider = order.getProvider();
            var status = order.getStatus();

            InternalServiceOrderFcl fcl = fclById.get(order.getId());
            InternalServiceOrderLcl lcl = lclById.get(order.getId());
            ScheduleFclDetailResponse fclResponse = fcl == null ? null : new ScheduleFclDetailResponse(
                    fcl.getContainerNumber(),
                    fcl.getCargoType() == null ? null : fcl.getCargoType().getName(),
                    fcl.getCubicMeters(),
                    fcl.getEmptyWarehouseLocation() == null ? null : fcl.getEmptyWarehouseLocation().getId(),
                    fcl.getEmptyWarehouseLocation() == null ? null : fcl.getEmptyWarehouseLocation().getName(),
                    fcl.getReturnDueDate(), fcl.getMemo());
            ScheduleLclDetailResponse lclResponse = lcl == null ? null : new ScheduleLclDetailResponse(
                    lcl.getPackages(), lcl.getWeightKg(), lcl.getCubicMeters());

            return new ScheduleOrderResponse(
                    order.getId(), order.getInternalOrderNumber(),
                    client == null ? null : client.getId(), client == null ? null : client.getBusinessName(),
                    clientOrder == null ? null : clientOrder.getId(),
                    clientOrder == null ? order.getClientOrderNumber() : clientOrder.getClientOrderNumber(),
                    order.getServiceType(), order.getReference(),
                    origin == null ? null : origin.getId(), origin == null ? null : origin.getName(),
                    destination == null ? null : destination.getId(), destination == null ? null : destination.getName(),
                    provider == null ? null : provider.getId(), provider == null ? null : provider.getBusinessName(),
                    order.getScheduledDate(), status == null ? null : status.getId(),
                    status == null ? null : status.getName(), order.getBillingStatus(), order.getObservations(),
                    fclResponse, lclResponse);
        }).toList();
    }

    private InternalServiceOrder buscar(Long id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "O/S interna no encontrada"));
    }

    private boolean esCancelada(InternalServiceOrder order) {
        return order.getStatus() != null && "Cancelada".equalsIgnoreCase(order.getStatus().getName());
    }

    private void validarPeriodo(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar un rango válido: startDate y endDate");
        }
    }

    private String key(Long orderId, LocalDate date) {
        return orderId + "|" + date;
    }

    public record ScheduleBoardResponse(LocalDate startDate, LocalDate endDate,
            List<ScheduleOrderResponse> scheduled, List<ScheduleOrderResponse> available) {}
}
