package com.lmoving.intranet.evaluation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EvaluationSubmitRequest(

    @NotEmpty(message = "Debe enviar al menos una respuesta")
    List<@Valid EvaluationAnswerRequest> answers

) {
}