package com.lmoving.intranet.activity.dto;

import java.time.LocalDateTime;

public record OperationActivityResponse(
    Long id, Long operationId, String actorUsername, String actorName,
    String action, String entityType, Long entityId, String description,
    String oldValue, String newValue, LocalDateTime createdAt
) {}
