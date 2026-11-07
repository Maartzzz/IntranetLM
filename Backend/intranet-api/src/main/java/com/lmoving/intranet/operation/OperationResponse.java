package com.lmoving.intranet.operation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OperationResponse(
    Long id, String operationNumber, String reference,
    Long clientId, String clientName,
    Long clientSubclientId, Long subclientId, String subclientName,
    Long cargoTypeId, String cargoTypeName, boolean container,
    String customerOrderNumber, String origin, String destination,
    Integer quantity, LocalDate estimatedDate,
    Long statusId, String statusName, String statusColor,
    String observation, LocalDateTime createdAt, LocalDateTime updatedAt
) {}
