export interface CourseAudience {
    id: number;
    courseId: number;
    role: string;
}

export interface CourseAudienceRequest {
    role: string;
}

export const COURSE_AUDIENCE_ROLES = [
    'ADMIN',
    'SUPERVISOR',
    'CHOFER',
    'OPERACIONES'
];