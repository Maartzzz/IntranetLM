package com.lmoving.intranet.dashboard;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmoving.intranet.dashboard.dto.DashboardResponse;
import com.lmoving.intranet.dashboard.dto.RecentEvaluationResponse;
import com.lmoving.intranet.user.User;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(
            DashboardRepository dashboardRepository
    ) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        List<RecentEvaluationResponse> recentEvaluations =
                dashboardRepository
                        .findRecentEvaluations(PageRequest.of(0, 5))
                        .stream()
                        .map(attempt -> {
        
                            User user = attempt.getUser();
        
                            return new RecentEvaluationResponse(
                                    attempt.getId(),
                                    user.getFirstName() + " " + user.getLastName(),
                                    user.getDocumentType().name(),
                                    user.getDocumentNumber(),
                                    attempt.getCourse().getTitle(),
                                    attempt.getScore(),
                                    Boolean.TRUE.equals(attempt.getPassed()),
                                    attempt.getCompletedAt()
                            );
                        })
                        .toList();

        return new DashboardResponse(
                dashboardRepository.countByActiveTrue(),
                dashboardRepository.countActiveDrivers(),
                dashboardRepository.countActiveCourses(),
                dashboardRepository.countCertificates(),
                dashboardRepository.countCompletedEvaluations(),
                dashboardRepository.calculateApprovalRate(),
                recentEvaluations
        );
    }

}