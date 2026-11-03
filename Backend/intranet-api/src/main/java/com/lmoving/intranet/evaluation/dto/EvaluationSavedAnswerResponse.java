package com.lmoving.intranet.evaluation.dto;

public record EvaluationSavedAnswerResponse(
        Long questionId,
        Long selectedOptionId
) {}
