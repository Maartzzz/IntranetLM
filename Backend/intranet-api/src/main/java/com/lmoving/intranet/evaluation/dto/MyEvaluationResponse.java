package com.lmoving.intranet.evaluation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyEvaluationResponse(
        Long attemptId,
        Long courseId,
        String courseTitle,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        BigDecimal score,
        Integer passingScore,
        Boolean passed
) {}
