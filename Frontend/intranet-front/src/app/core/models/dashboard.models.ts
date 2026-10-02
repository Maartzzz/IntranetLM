export interface RecentEvaluation {
  attemptId: number;
  fullName: string;
  documentType: string;
  documentNumber: string;
  courseTitle: string;
  score: number;
  passed: boolean;
  completedAt: string;
}

export interface Dashboard {
  totalUsers: number;
  totalDrivers: number;
  activeCourses: number;
  certificates: number;
  completedEvaluations: number;
  approvalRate: number;
  recentEvaluations: RecentEvaluation[];
}