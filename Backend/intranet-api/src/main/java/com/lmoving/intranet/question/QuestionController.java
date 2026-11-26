package com.lmoving.intranet.question;

import org.springframework.security.access.prepost.PreAuthorize;
import com.lmoving.intranet.question.dto.QuestionRequest;
import com.lmoving.intranet.question.dto.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PreAuthorize("hasAuthority('TRAINING.QUESTIONS.VIEW')")
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAll() {
        return ResponseEntity.ok(questionService.getAll());
    }

    @PreAuthorize("hasAuthority('TRAINING.QUESTIONS.VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @PreAuthorize("hasAuthority('TRAINING.QUESTIONS.CREATE')")
    @PostMapping
    public ResponseEntity<QuestionResponse> create(
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(questionService.create(request));
    }

    @PreAuthorize("hasAuthority('TRAINING.QUESTIONS.EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(
            questionService.update(id, request)
        );
    }

    @PreAuthorize("hasAuthority('TRAINING.QUESTIONS.EDIT')")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<QuestionResponse> toggleActive(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            questionService.toggleActive(id)
        );
    }

    @PreAuthorize("hasAuthority('TRAINING.QUESTIONS.DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
