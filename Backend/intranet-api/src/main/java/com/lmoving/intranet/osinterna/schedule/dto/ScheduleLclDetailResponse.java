package com.lmoving.intranet.osinterna.schedule.dto;

import java.math.BigDecimal;

public record ScheduleLclDetailResponse(
    Integer packages,
    BigDecimal weightKg,
    BigDecimal cubicMeters
) {}