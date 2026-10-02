package com.lmoving.intranet.materials.dto;

import com.lmoving.intranet.materials.MaterialType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseMaterialRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(
        max = 150,
        message = "El título no puede superar los 150 caracteres"
    )
    private String title;

    private String description;

    @NotNull(message = "El tipo de material es obligatorio")
    private MaterialType type;

    @NotBlank(message = "La URL es obligatoria")
    private String url;

    private boolean required;

    private boolean active = true;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MaterialType getType() {
        return type;
    }

    public void setType(MaterialType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}