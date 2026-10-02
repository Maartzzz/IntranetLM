package com.lmoving.intranet.courses.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    String title,

    String description,

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser mayor a 0")
    Integer durationMinutes,

    @NotNull(message = "La nota mínima es obligatoria")
    @Min(value = 0, message = "La nota mínima no puede ser menor a 0")
    @Max(value = 100, message = "La nota mínima no puede ser mayor a 100")
    Integer passingScore,

    @NotNull(message = "La cantidad de preguntas es obligatoria")
    @Min(value = 1, message = "Debe existir al menos una pregunta")
    Integer evaluationQuestionCount,

    Boolean active  

) {
}