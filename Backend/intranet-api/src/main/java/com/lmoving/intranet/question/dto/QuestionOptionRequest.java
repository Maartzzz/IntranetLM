package com.lmoving.intranet.question.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionOptionRequest(

    @NotBlank(message = "El texto de la opción es obligatorio")
    String optionText,

    boolean correct

) {
}