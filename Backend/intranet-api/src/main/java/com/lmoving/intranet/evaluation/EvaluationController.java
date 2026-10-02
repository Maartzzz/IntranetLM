package com.lmoving.intranet.evaluation;

import com.lmoving.intranet.evaluation.dto.EvaluationAttemptResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationResultResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationStartResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationSubmitRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/courses/{courseId}/start")
    public ResponseEntity<EvaluationStartResponse> startEvaluation(
        @PathVariable Long courseId,
        Principal principal
    ) {
        return ResponseEntity.ok(
            evaluationService.startEvaluation(
                courseId,
                principal.getName()
            )
        );
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<EvaluationResultResponse> submitEvaluation(
            @PathVariable Long attemptId,
            @Valid @RequestBody EvaluationSubmitRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(
                evaluationService.submitEvaluation(
                        attemptId,
                        principal.getName(),
                        request
                )
        );
    }

    @DeleteMapping("/attempts/{attemptId}")
    public ResponseEntity<Void> cancelEvaluation(
            @PathVariable Long attemptId,
            Principal principal
    ) {
        evaluationService.cancelEvaluation(
                attemptId,
                principal.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/attempts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<EvaluationAttemptResponse>> getCompletedAttemptsByCourse(
            @PathVariable Long courseId
    ) {
    
        return ResponseEntity.ok(
                evaluationService.getCompletedAttemptsByCourse(courseId)
        );
    }
}