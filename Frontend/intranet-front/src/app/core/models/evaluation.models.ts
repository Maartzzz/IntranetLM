export interface EvaluationQuestionOption {
    id: number;
    optionText: string;
}

export interface EvaluationQuestion {
    questionId: number;
    questionText: string;
    options: EvaluationQuestionOption[];
}

export interface EvaluationStartResponse {
    attemptId: number;
    courseId: number;
    courseTitle: string;
    passingScore: number;
    totalQuestions: number;
    questions: EvaluationQuestion[];
}

export interface EvaluationAnswerRequest {
    questionId: number;
    selectedOptionId: number | null;
}

export interface EvaluationSubmitRequest {
    answers: EvaluationAnswerRequest[];
}

export interface EvaluationResultResponse {
    attemptId: number;
    courseId: number;
    courseTitle: string;
    startedAt: string;
    completedAt: string;
    totalQuestions: number;
    correctAnswers: number;
    score: number;
    passingScore: number;
    passed: boolean;
}

export interface EvaluationAttempt {
    attemptId: number;
    username: string;
    fullName: string;
    documentType: string;
    documentNumber: string;
    startedAt: string;
    completedAt: string;
    totalQuestions: number;
    correctAnswers: number;
    score: number;
    passingScore: number;
    passed: boolean;
}


export interface PendingEvaluation {
    attemptId: number;
    courseId: number;
    courseTitle: string;
    username: string;
    fullName: string;
    documentType: string;
    documentNumber: string;
    roundNumber: number;
    startedAt: string;
}

export interface EvaluationAnswerProgressRequest {
    questionId: number;
    selectedOptionId: number | null;
}

export interface EvaluationSavedAnswer {
    questionId: number;
    selectedOptionId: number | null;
}

export interface EvaluationResumeResponse {
    attemptId: number;
    courseId: number;
    courseTitle: string;
    passingScore: number;
    totalQuestions: number;
    questions: EvaluationQuestion[];
    savedAnswers: EvaluationSavedAnswer[];
}

export interface MyEvaluationResponse {
    attemptId: number;
    courseId: number;
    courseTitle: string;
    startedAt: string;
    completedAt: string | null;
    score: number | null;
    passingScore: number;
    passed: boolean | null;
}
