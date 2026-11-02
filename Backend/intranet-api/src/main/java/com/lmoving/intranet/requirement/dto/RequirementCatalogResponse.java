package com.lmoving.intranet.requirement.dto;

import java.time.LocalDateTime;

public record RequirementCatalogResponse(
    Long id, String name, String description, boolean active,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}
