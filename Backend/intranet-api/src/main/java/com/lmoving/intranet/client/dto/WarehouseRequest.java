package com.lmoving.intranet.client.dto;

import com.lmoving.intranet.client.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
    @NotNull Long locationId,
    @NotBlank @Size(max = 30) String warehouseCode,
    @NotBlank @Size(max = 150) String warehouseName,
    @NotNull WarehouseType warehouseType,
    @NotBlank @Size(max = 150) String operatorName,
    @Size(max = 5000) String observations
) {}
