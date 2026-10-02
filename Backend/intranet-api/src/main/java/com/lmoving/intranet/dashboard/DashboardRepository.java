package com.lmoving.intranet.dashboard;

import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.evaluation.EvaluationAttempt;
import com.lmoving.intranet.user.User;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DashboardRepository
        extends JpaRepository<User, String> {

    long countByActiveTrue();

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.role = com.lmoving.intranet.user.Role.CHOFER
            AND u.active = true
    """)
    long countActiveDrivers();

    @Query("""
        SELECT COUNT(c)
        FROM Courses c
        WHERE c.active = true
    """)
    long countActiveCourses();

    @Query("""
        SELECT COUNT(a)
        FROM EvaluationAttempt a
        WHERE a.passed = true
            AND a.completedAt IS NOT NULL
    """)
    long countCertificates();

    @Query("""
        SELECT COUNT(a)
        FROM EvaluationAttempt a
        WHERE a.completedAt IS NOT NULL
    """)
    long countCompletedEvaluations();

    @Query("""
        SELECT
            CASE
                WHEN COUNT(a) = 0 THEN 0.0
                ELSE
                    (SUM(
                        CASE
                            WHEN a.passed = true THEN 1
                            ELSE 0
                        END
                    ) * 100.0) / COUNT(a)
            END
        FROM EvaluationAttempt a
        WHERE a.completedAt IS NOT NULL
    """)
    double calculateApprovalRate();

    @Query("""
        SELECT a
        FROM EvaluationAttempt a
        WHERE a.completedAt IS NOT NULL
        ORDER BY a.completedAt DESC
        """)
    List<EvaluationAttempt> findRecentEvaluations(Pageable pageable);
}