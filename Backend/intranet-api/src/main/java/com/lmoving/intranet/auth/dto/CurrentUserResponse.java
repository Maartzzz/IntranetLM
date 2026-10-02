package com.lmoving.intranet.auth.dto;

public record CurrentUserResponse(
    String username,
    String firstName,
    String lastName,
    String role
) {}