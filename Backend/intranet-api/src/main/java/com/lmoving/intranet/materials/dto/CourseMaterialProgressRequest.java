package com.lmoving.intranet.materials.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CourseMaterialProgressRequest {

    @NotNull(message = "El porcentaje de progreso es obligatorio")
    @Min(value = 0, message = "El progreso no puede ser menor a 0")
    @Max(value = 100, message = "El progreso no puede ser mayor a 100")
    private Integer progressPercent;

    private Long lastPosition;

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Long lastPosition) {
        this.lastPosition = lastPosition;
    }
}