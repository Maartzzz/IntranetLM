package com.lmoving.intranet.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuestionRequest(

    @NotBlank(message = "El texto de la pregunta es obligatorio")
    String questionText,

    boolean active,

    @NotEmpty(message = "La pregunta debe tener al menos una opción")
    List<@Valid QuestionOptionRequest> options

) {
}