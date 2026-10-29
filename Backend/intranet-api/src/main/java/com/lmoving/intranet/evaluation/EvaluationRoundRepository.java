package com.lmoving.intranet.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EvaluationRoundRepository
        extends JpaRepository<EvaluationRound, Long> {

    Optional<EvaluationRound>
    findFirstByCourse_IdAndUser_UsernameOrderByRoundNumberDesc(
        Long courseId,
        String username
    );
    
    boolean existsByCourse_IdAndUser_UsernameAndRoundNumber(
        Long courseId,
        String username,
        Integer roundNumber
    );

    @Query("""
        SELECT r
        FROM EvaluationRound r
        WHERE r.roundNumber = (
            SELECT MAX(r2.roundNumber)
            FROM EvaluationRound r2
            WHERE r2.course.id = r.course.id
            AND r2.user.username = r.user.username
        )
        AND (
            SELECT COUNT(a)
            FROM EvaluationAttempt a
            WHERE a.round.id = r.id
            AND a.completedAt IS NOT NULL
        ) >= 3
        AND NOT EXISTS (
            SELECT a
            FROM EvaluationAttempt a
            WHERE a.round.id = r.id
            AND a.passed = true
            AND a.completedAt IS NOT NULL
        )
        AND NOT EXISTS (
            SELECT a
            FROM EvaluationAttempt a
            WHERE a.round.id = r.id
            AND a.completedAt IS NULL
        )
        ORDER BY r.createdAt DESC
        """)
    List<EvaluationRound> findExhaustedCurrentRounds();
}
