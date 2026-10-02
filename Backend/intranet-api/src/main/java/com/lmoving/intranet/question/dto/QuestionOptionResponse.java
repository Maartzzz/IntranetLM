package com.lmoving.intranet.question.dto;

public record QuestionOptionResponse(
    Long id,
    String optionText,
    boolean correct
) {
}