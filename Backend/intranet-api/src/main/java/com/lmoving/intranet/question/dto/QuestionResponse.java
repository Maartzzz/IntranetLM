package com.lmoving.intranet.question.dto;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionResponse(
    Long id,
    String questionText,
    boolean active,
    List<QuestionOptionResponse> options,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}