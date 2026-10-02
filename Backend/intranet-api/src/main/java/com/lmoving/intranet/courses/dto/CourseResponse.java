package com.lmoving.intranet.courses.dto;

import java.time.LocalDateTime;

public record CourseResponse(

    Long id,
    String title,
    String description,
    Integer durationMinutes,
    Integer passingScore,
    Integer evaluationQuestionCount,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {
}