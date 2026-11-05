package com.lmoving.intranet.osinterna.schedule.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduleFclDetailResponse(
    String containerNumber,
    String cargoTypeName,
    BigDecimal cubicMeters,
    Long emptyWarehouseLocationId,
    String emptyWarehouseLocationName,
    LocalDate returnDueDate,
    String memo
) {}