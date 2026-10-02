package com.lmoving.intranet.dashboard.dto;

import java.util.List;

public record DashboardResponse(
        long totalUsers,
        long totalDrivers,
        long activeCourses,
        long certificates,
        long completedEvaluations,
        double approvalRate,
        List<RecentEvaluationResponse> recentEvaluations
) {
}