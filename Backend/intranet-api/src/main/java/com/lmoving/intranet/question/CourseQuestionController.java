package com.lmoving.intranet.question;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/questions")
public class CourseQuestionController {

    private final CourseQuestionService courseQuestionService;

    private final QuestionImportService questionImportService;

    public CourseQuestionController(
        CourseQuestionService courseQuestionService, QuestionImportService questionImportService
    ) {
        this.courseQuestionService = courseQuestionService;
        this.questionImportService = questionImportService;
    }

    @GetMapping
    public ResponseEntity<List<Question>> getQuestionsByCourse(
        @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
            courseQuestionService.getQuestionsByCourse(courseId)
        );
    }

    @PostMapping("/{questionId}")
    public ResponseEntity<Void> assignQuestion(
        @PathVariable Long courseId,
        @PathVariable Long questionId
    ) {
        courseQuestionService.assignQuestion(courseId, questionId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> removeQuestion(
        @PathVariable Long courseId,
        @PathVariable Long questionId
    ) {
        courseQuestionService.removeQuestion(courseId, questionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<?> importQuestions(
        @PathVariable Long courseId,
        @RequestParam("file") MultipartFile file
    ) {
    
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("El archivo está vacío.");
        }
    
        try {
    
            var rows = questionImportService.importQuestions(
                courseId,
                file.getInputStream()
            );
    
            return ResponseEntity.ok(
                java.util.Map.of(
                    "message", "Preguntas importadas Correctamente.",
                    "rows", rows.size()
                )
            );
    
        } catch (IllegalArgumentException e) {
    
            return ResponseEntity.badRequest()
                .body(e.getMessage());
    
        } catch (Exception e) {
    
            return ResponseEntity.internalServerError()
                .body("No se pudo procesar el archivo.");
        }
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {

        byte[] file =
            questionImportService.generateTemplate();

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=plantilla_preguntas.xlsx"
            )
            .contentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
            .body(file);
    }
    
}