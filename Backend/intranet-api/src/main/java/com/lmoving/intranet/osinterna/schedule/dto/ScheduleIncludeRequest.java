package com.lmoving.intranet.osinterna.schedule.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScheduleIncludeRequest(
        @NotNull LocalDate scheduledDate,
        @Size(max = 1000) String reason) {}
