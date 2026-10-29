package com.lmoving.intranet.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationAttemptRepository extends JpaRepository<EvaluationAttempt, Long> {

    List<EvaluationAttempt> findByCourseIdAndUsernameOrderByStartedAtDesc(
        Long courseId,
        Long username
    );

    List<EvaluationAttempt> findByUsernameOrderByStartedAtDesc(Long username);

    List<EvaluationAttempt> findByPassedTrueAndCompletedAtIsNotNullOrderByCompletedAtDesc();

    @Query("""
        SELECT a
        FROM EvaluationAttempt a
        WHERE a.course.id = :courseId
        AND a.completedAt IS NOT NULL
        AND a.completedAt = (
            SELECT MAX(a2.completedAt)
            FROM EvaluationAttempt a2
            WHERE a2.course.id = :courseId
                AND a2.username.username = a.username.username
                AND a2.completedAt IS NOT NULL
        )
        ORDER BY a.completedAt DESC
        """)
    List<EvaluationAttempt> findLatestCompletedAttemptByCourse(
            @Param("courseId") Long courseId
    );

    long countByRound_IdAndCompletedAtIsNotNull(Long roundId);

    boolean existsByRound_IdAndCompletedAtIsNull(Long roundId);

    boolean existsByRound_IdAndPassedTrueAndCompletedAtIsNotNull(
        Long roundId
    );

    Optional<EvaluationAttempt>
    findFirstByRound_IdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long roundId);
    
    List<EvaluationAttempt> findByCompletedAtIsNullOrderByStartedAtAsc();

    List<EvaluationAttempt>
    findByUsername_UsernameOrderByStartedAtDesc(String username);
}