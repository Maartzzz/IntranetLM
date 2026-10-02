package com.lmoving.intranet.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationAnswerRepository extends JpaRepository<EvaluationAnswer, Long> {

    List<EvaluationAnswer> findByAttemptId(Long attemptId);

    boolean existsByAttemptIdAndSelectedOptionIsNotNull(Long attemptId);
    
}