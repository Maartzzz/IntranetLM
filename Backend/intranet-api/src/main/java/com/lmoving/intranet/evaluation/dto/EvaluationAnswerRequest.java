package com.lmoving.intranet.evaluation.dto;

import jakarta.validation.constraints.NotNull;

public record EvaluationAnswerRequest(

    @NotNull(message = "La pregunta es obligatoria")
    Long questionId,

    Long selectedOptionId

) {
}