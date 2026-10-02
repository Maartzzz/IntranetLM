package com.lmoving.intranet.evaluation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvaluationAttemptResponse(

    Long attemptId,

    String username,

    String fullName,

    String documentType,

    String documentNumber,

    LocalDateTime startedAt,

    LocalDateTime completedAt,

    Integer totalQuestions,

    Integer correctAnswers,

    BigDecimal score,

    Integer passingScore,

    boolean passed

) {
}