package com.lmoving.intranet.evaluation.dto;

import java.time.LocalDateTime;

public record PendingEvaluationResponse(
        Long attemptId,
        Long courseId,
        String courseTitle,
        String username,
        String fullName,
        String documentType,
        String documentNumber,
        Integer roundNumber,
        LocalDateTime startedAt
) {}
