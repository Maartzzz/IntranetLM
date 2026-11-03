package com.lmoving.intranet.client.dto;

import com.lmoving.intranet.client.ClientDocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientRequest(
    @NotNull(message = "El tipo de documento es obligatorio")
    ClientDocumentType documentType,

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede superar los 20 caracteres")
    String documentNumber,

    @NotBlank(message = "La razón social o nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    String businessName,

    @Size(max = 300, message = "La dirección no puede superar los 300 caracteres")
    String address
) {}
