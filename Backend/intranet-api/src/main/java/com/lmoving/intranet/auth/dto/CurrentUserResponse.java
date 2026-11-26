package com.lmoving.intranet.auth.dto;

import java.util.List;

public record CurrentUserResponse(
    String username,
    String firstName,
    String lastName,
    String role,
    List<String> permissions
) {}
