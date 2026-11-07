package com.lmoving.intranet.operation;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
public record OperationRequest(
    @Size(max = 150) String reference,
    @NotNull Long clientId,
    Long clientSubclientId,
    @NotNull Long cargoTypeId,
    @Size(max = 100) String customerOrderNumber,
    @Size(max = 200) String origin,
    @Size(max = 200) String destination,
    @Positive Integer quantity,
    LocalDate estimatedDate,
    Long statusId,
    @Size(max = 1000) String observation
) {}
