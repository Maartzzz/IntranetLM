package com.lmoving.intranet.evaluation;

import com.lmoving.intranet.courses.dto.CourseResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationAnswerProgressRequest;
import com.lmoving.intranet.evaluation.dto.EvaluationAttemptResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationResultResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationResumeResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationStartResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationSubmitRequest;
import com.lmoving.intranet.evaluation.dto.ExhaustedEvaluationResponse;
import com.lmoving.intranet.evaluation.dto.MyEvaluationResponse;
import com.lmoving.intranet.evaluation.dto.PendingEvaluationResponse;

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

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.EDIT')")
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

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.EDIT')")
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

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.EDIT')")
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

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.ADMIN')")
    @GetMapping("/courses/{courseId}/attempts")
    public ResponseEntity<List<EvaluationAttemptResponse>> getCompletedAttemptsByCourse(
            @PathVariable Long courseId
    ) {
    
        return ResponseEntity.ok(
                evaluationService.getCompletedAttemptsByCourse(courseId)
        );
    }

    
    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.ADMIN')")
    @PostMapping("/courses/{courseId}/users/{username}/rehabilitate")
    public ResponseEntity<Void> rehabilitateEvaluation(
            @PathVariable Long courseId,
            @PathVariable String username,
            Principal principal
    ) {
        evaluationService.rehabilitateEvaluation(
            courseId,
            username,
            principal.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.ADMIN')")
    @GetMapping("/exhausted")
    public ResponseEntity<List<ExhaustedEvaluationResponse>>
    getExhaustedEvaluations() {
        return ResponseEntity.ok(
                evaluationService.getExhaustedEvaluations()
        );
    }

    
    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.VIEW')")
    @GetMapping("/pending")
    public ResponseEntity<List<PendingEvaluationResponse>> getPendingEvaluations(
            Principal principal
    ) {
        return ResponseEntity.ok(
                evaluationService.getPendingEvaluations(
                        principal.getName()
                )
        );
    }

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.EDIT')")
    @GetMapping("/attempts/{attemptId}/resume")
    public ResponseEntity<EvaluationResumeResponse> resumeEvaluation(
            @PathVariable Long attemptId,
            Principal principal
    ) {
        return ResponseEntity.ok(
                evaluationService.resumeEvaluation(
                        attemptId,
                        principal.getName()
                )
        );
    }
    
    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.EDIT')")
    @PutMapping("/attempts/{attemptId}/answers")
    public ResponseEntity<Void> saveAnswerProgress(
            @PathVariable Long attemptId,
            @RequestBody EvaluationAnswerProgressRequest request,
            Principal principal
    ) {
        evaluationService.saveAnswerProgress(
                attemptId,
                principal.getName(),
                request
        );
    
        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.VIEW')")
    @GetMapping("/mine")
    public ResponseEntity<List<MyEvaluationResponse>> getMyEvaluations(
            Principal principal
    ) {
        return ResponseEntity.ok(
                evaluationService.getMyEvaluations(
                        principal.getName()
                )
        );
    }

    @PreAuthorize("hasAuthority('TRAINING.EVALUATIONS.VIEW')")
    @GetMapping("/available")
    public ResponseEntity<List<CourseResponse>> getAvailableEvaluationCourses(
            Principal principal) {
        return ResponseEntity.ok(
                evaluationService.getAvailableEvaluationCourses(principal.getName())
        );
    }
}
