package com.lmoving.intranet.evaluation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvaluationResultResponse(

    Long attemptId,
    Long courseId,
    String courseTitle,

    LocalDateTime startedAt,
    LocalDateTime completedAt,

    Integer totalQuestions,
    Integer correctAnswers,
    BigDecimal score,

    Integer passingScore,
    boolean passed

) {
}