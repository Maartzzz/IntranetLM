package com.lmoving.intranet.evaluation.dto;

import java.util.List;

public record EvaluationResumeResponse(
        Long attemptId,
        Long courseId,
        String courseTitle,
        Integer passingScore,
        Integer totalQuestions,
        List<EvaluationQuestionResponse> questions,
        List<EvaluationSavedAnswerResponse> savedAnswers
) {}
