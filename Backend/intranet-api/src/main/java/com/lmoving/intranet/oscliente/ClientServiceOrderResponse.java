package com.lmoving.intranet.oscliente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClientServiceOrderResponse(
    Long id, String clientOrderNumber, Long clientId, String clientName,
    Long operationId, String operationNumber, String reference, String serviceType,
    LocalDate receivedDate, LocalDate estimatedArrivalDate, String origin, String destination,
    String observation, Long statusId, String statusName, String statusColor,
    List<FclQuantityResponse> fclQuantities, Integer totalPackages,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public record FclQuantityResponse(Long cargoTypeId, String cargoTypeName, Integer totalQuantity) {}
}
