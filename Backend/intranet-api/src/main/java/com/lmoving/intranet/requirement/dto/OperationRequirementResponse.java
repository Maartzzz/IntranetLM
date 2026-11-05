package com.lmoving.intranet.requirement.dto;

import java.time.LocalDateTime;

public record OperationRequirementResponse(
    Long id,
    Long operationId,
    Long clientServiceOrderId,
    Long internalServiceOrderId,
    Long requirementCatalogId,
    String requirementName,
    String externalResponsible,
    Long statusId,
    String statusName,
    String statusColor,
    String comment,
    LocalDateTime registeredAt,
    LocalDateTime updatedAt
) {}