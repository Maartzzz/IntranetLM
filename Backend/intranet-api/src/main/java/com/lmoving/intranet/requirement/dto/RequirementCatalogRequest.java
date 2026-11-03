package com.lmoving.intranet.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequirementCatalogRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 4000) String description,
    Boolean active
) {}
