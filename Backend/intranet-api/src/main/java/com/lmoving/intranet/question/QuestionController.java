package com.lmoving.intranet.question;

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

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAll() {
        return ResponseEntity.ok(questionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(questionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(
            questionService.update(id, request)
        );
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<QuestionResponse> toggleActive(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            questionService.toggleActive(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}