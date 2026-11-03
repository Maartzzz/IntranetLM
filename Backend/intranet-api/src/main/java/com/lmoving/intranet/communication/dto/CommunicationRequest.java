
package com.lmoving.intranet.communication.dto;

import com.lmoving.intranet.communication.CommunicationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommunicationRequest {

    @NotNull(message = "Debes seleccionar el tipo de comunicación")
    private CommunicationType type;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(
        max = 150,
        message = "El asunto no puede superar los 150 caracteres"
    )
    private String subject;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(
        max = 10000,
        message = "La descripción no puede superar los 10000 caracteres"
    )
    private String description;

    public CommunicationType getType() {
        return type;
    }

    public void setType(CommunicationType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
