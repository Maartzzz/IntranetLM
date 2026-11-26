package com.lmoving.intranet.operation.archive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lmoving.intranet.audit.AuditLogService;
import com.lmoving.intranet.operation.Operation;
import com.lmoving.intranet.operation.OperationRepository;
import com.lmoving.intranet.oscliente.ClientServiceOrder;
import com.lmoving.intranet.oscliente.ClientServiceOrderRepository;
import com.lmoving.intranet.osinterna.InternalServiceOrder;
import com.lmoving.intranet.osinterna.InternalServiceOrderRepository;

@Service
@Transactional
public class OperationArchiveService {
    private final OperationRepository operations;
    private final ClientServiceOrderRepository clientOrders;
    private final InternalServiceOrderRepository internalOrders;
    private final AuditLogService audit;

    public OperationArchiveService(OperationRepository operations,
            ClientServiceOrderRepository clientOrders,
            InternalServiceOrderRepository internalOrders, AuditLogService audit) {
        this.operations = operations; this.clientOrders = clientOrders;
        this.internalOrders = internalOrders; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<OperationArchiveResponse> list(String status, String search) {
        List<Operation> rows = operations.findAllByArchivedTrueOrderByArchivedAtDesc();
        return rows.stream()
            .filter(o -> status == null || status.isBlank() || status.equalsIgnoreCase("TODOS")
                || (o.getStatus() != null && o.getStatus().getName().equalsIgnoreCase(status)))
            .filter(o -> search == null || search.isBlank() || searchable(o).contains(search.trim().toLowerCase(Locale.ROOT)))
            .map(this::toResponse).toList();
    }

    public OperationArchiveResponse restore(Long id) {
        Operation operation = operations.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada"));
        if (!operation.isArchived())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La operación no está archivada");
        String old = "Archivada=" + operation.getArchivedAt() + "; Motivo=" + operation.getArchiveReason();
        operation.setArchived(false); operation.setArchivedAt(null); operation.setArchiveReason(null);
        operations.saveAndFlush(operation);
        audit.registrar("OPERACIONES", "OPERACION", operation.getId(), "RESTORED",
            "Se restauró la operación " + operation.getOperationNumber(), old, "Archivada=false");
        return toResponse(operation);
    }

    public int archiveEligible() {
        LocalDateTime now = LocalDateTime.now();
        List<Operation> rows = operations.findAllByArchivedFalseOrderByCreatedAtDesc();
        int count = 0;
        for (Operation operation : rows) {
            if (operation.getStatus() == null) continue;
            String status = operation.getStatus().getName();
            String reason = null;
            if ("Cancelada".equalsIgnoreCase(status)) reason = "CANCELACION";
            else if ("Finalizada".equalsIgnoreCase(status)
                    && operation.getStatusChangedAt() != null
                    && !operation.getStatusChangedAt().isAfter(now.minusDays(30))) reason = "FINALIZACION";
            if (reason == null || operations.hasPendingDistribution(operation.getId())) continue;
            operation.setArchived(true); operation.setArchivedAt(now); operation.setArchiveReason(reason);
            operations.save(operation);
            audit.registrarSistema("OPERACIONES", "OPERACION", operation.getId(), "ARCHIVED",
                "Se archivó automáticamente la operación " + operation.getOperationNumber(),
                "Estado=" + status, "Motivo=" + reason + "; Fecha=" + now);
            count++;
        }
        return count;
    }

    private String searchable(Operation o) {
        ClientServiceOrder cso = clientOrders.findByOperation_Id(o.getId()).orElse(null);
        return String.join(" ", Objects.toString(o.getOperationNumber(), ""),
            Objects.toString(o.getReference(), ""), Objects.toString(o.getClient().getBusinessName(), ""),
            cso == null ? "" : Objects.toString(cso.getClientOrderNumber(), "")).toLowerCase(Locale.ROOT);
    }

    private OperationArchiveResponse toResponse(Operation o) {
        ClientServiceOrder cso = clientOrders.findByOperation_Id(o.getId()).orElse(null);
        List<InternalServiceOrder> internals = cso == null ? List.of()
            : internalOrders.findAllByClientServiceOrder_IdOrderByCreatedAtDesc(cso.getId());
        return new OperationArchiveResponse(o.getId(), o.getOperationNumber(), o.getReference(),
            o.getClient().getBusinessName(), cso == null ? null : cso.getClientOrderNumber(),
            internals.size(), internals.stream().map(x -> "OSI-" + x.getInternalOrderNumber()).toList(),
            o.getCargoType().getName(), o.getCargoType().isContainer(), o.getEstimatedDate(),
            o.getStatus() == null ? null : o.getStatus().getName(), o.getArchivedAt(), o.getArchiveReason());
    }
}
