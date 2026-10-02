package com.lmoving.intranet.question.dto;

public record QuestionImportRow(

    String question,
    String option1,
    String option2,
    String option3,
    String option4,
    Integer correct,
    Boolean active

) {
}