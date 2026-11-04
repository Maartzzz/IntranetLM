package com.lmoving.intranet.client.dto;

import java.time.LocalDateTime;
import com.lmoving.intranet.client.Warehouse;
import com.lmoving.intranet.client.WarehouseType;

public record WarehouseResponse(
    Long id, Long locationId, String locationName, String address, String district, String province,
    String warehouseCode, String warehouseName, WarehouseType warehouseType, String operatorName,
    boolean active, String observations, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static WarehouseResponse from(Warehouse w) {
        var l = w.getLocation();
        return new WarehouseResponse(w.getId(), l.getId(), l.getName(), l.getAddress(), l.getDistrict(), l.getProvince(),
            w.getWarehouseCode(), w.getWarehouseName(), w.getWarehouseType(), w.getOperatorName(),
            w.isActive(), w.getObservations(), w.getCreatedAt(), w.getUpdatedAt());
    }
}
