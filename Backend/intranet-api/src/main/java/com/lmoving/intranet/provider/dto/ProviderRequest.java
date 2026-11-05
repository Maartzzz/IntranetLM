package com.lmoving.intranet.provider.dto;

import com.lmoving.intranet.provider.ProviderDocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProviderRequest(
    @NotNull ProviderDocumentType documentType,
    @NotBlank @Size(min = 8, max = 11) String documentNumber,
    @NotBlank @Size(max = 200) String businessName,
    @Size(max = 150) String contactName,
    @Size(max = 30) String phone,
    @Email @Size(max = 150) String email,
    @Size(max = 300) String address
) {}
