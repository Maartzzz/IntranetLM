package com.lmoving.intranet.requirement.dto;

import jakarta.validation.constraints.Size;

public record OperationRequirementRequest(
    Long requirementCatalogId,
    @Size(max = 120) String customName,
    @Size(max = 150) String externalResponsible,
    Long statusId,
    @Size(max = 10000) String comment
) {}
