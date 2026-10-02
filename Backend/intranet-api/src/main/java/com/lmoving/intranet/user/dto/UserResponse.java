package com.lmoving.intranet.user.dto;

import com.lmoving.intranet.user.DocumentType;
import com.lmoving.intranet.user.Role;

public record UserResponse(
        String username,
        String email,
        String firstName,
        String lastName,
        DocumentType documentType,
        String documentNumber,
        String phone,
        Role role,
        boolean active
) {
}