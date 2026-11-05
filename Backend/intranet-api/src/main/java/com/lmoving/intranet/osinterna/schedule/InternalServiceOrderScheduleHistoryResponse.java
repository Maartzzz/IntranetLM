package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InternalServiceOrderScheduleHistoryResponse(
    Long id, Long internalServiceOrderId, String eventType,
    LocalDate previousDate, LocalDate newDate, String reason,
    String actorUsername, String actorName, LocalDateTime createdAt
) {}
