package com.lmoving.intranet.osinterna.schedule.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ScheduleVersionDetailResponse(Long id, Integer versionNumber, LocalDate startDate, LocalDate endDate,
 String createdByUsername, String createdByName, LocalDateTime createdAt, List<Map<String, Object>> snapshot) {}
