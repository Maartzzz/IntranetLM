package com.lmoving.intranet.osinterna.schedule.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record ScheduleVersionSummaryResponse(Long id, Integer versionNumber, LocalDate startDate, LocalDate endDate,
 String createdByUsername, String createdByName, LocalDateTime createdAt, Integer scheduledOrderCount) {}
