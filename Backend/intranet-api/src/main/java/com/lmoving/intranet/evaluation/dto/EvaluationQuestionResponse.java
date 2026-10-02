package com.lmoving.intranet.evaluation.dto;

import java.util.List;

public record EvaluationQuestionResponse(
    Long questionId,
    String questionText,
    List<EvaluationQuestionOptionResponse> options
) {
}