package com.lmoving.intranet.evaluation.dto;

import java.time.LocalDateTime;

public record ExhaustedEvaluationResponse(
        Long courseId,
        String courseTitle,
        String username,
        String fullName,
        String documentType,
        String documentNumber,
        Integer roundNumber,
        long completedAttempts,
        LocalDateTime lastAttemptAt
) {}