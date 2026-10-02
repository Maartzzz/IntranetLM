package com.lmoving.intranet.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentEvaluationResponse(
        Long attemptId,
        String fullName,
        String documentType,
        String documentNumber,
        String courseTitle,
        BigDecimal score,
        boolean passed,
        LocalDateTime completedAt
) {
}