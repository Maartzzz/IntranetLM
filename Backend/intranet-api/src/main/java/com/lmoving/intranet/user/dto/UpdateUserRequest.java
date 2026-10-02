package com.lmoving.intranet.user.dto;

import com.lmoving.intranet.user.DocumentType;
import com.lmoving.intranet.user.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull
        DocumentType documentType,

        @NotBlank
        @Size(max = 20)
        String documentNumber,

        @Size(max = 20)
        String phone,

        @NotNull
        Role role
) {
}