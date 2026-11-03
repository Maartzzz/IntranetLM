package com.lmoving.intranet.evaluation.dto;

public record EvaluationAnswerProgressRequest(
        Long questionId,
        Long selectedOptionId
) {}