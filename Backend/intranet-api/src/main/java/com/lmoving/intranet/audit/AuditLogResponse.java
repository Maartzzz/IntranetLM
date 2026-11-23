package com.lmoving.intranet.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String actorUsername, String actorName, String module,
        String entityType, Long entityId, String entityKey, String action, String description,
        String oldValue, String newValue, LocalDateTime createdAt) {}
