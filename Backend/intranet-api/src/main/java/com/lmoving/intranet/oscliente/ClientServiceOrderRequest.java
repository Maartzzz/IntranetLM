package com.lmoving.intranet.oscliente;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record ClientServiceOrderRequest(
    @NotBlank @Size(max = 80) String clientOrderNumber,
    @NotNull Long clientId,
    Long operationId,
    @Size(max = 120) String reference,
    @NotBlank @Pattern(regexp = "FCL|LCL") String serviceType,
    LocalDate receivedDate,
    LocalDate estimatedArrivalDate,
    @Size(max = 200) String origin,
    @Size(max = 200) String destination,
    String observation,
    Long statusId,
    @Valid List<FclQuantityRequest> fclQuantities,
    @Min(0) Integer totalPackages
) {
    public record FclQuantityRequest(@NotNull Long cargoTypeId, @NotNull @Positive Integer totalQuantity) {}
}
