export interface Course {
    id: number;
    title: string;
    description: string | null;
    durationMinutes: number;
    passingScore: number;
    evaluationQuestionCount: number;
    active: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CourseRequest {
    title: string;
    description: string | null;
    durationMinutes: number;
    passingScore: number;
    evaluationQuestionCount: number;
    active?: boolean;
}