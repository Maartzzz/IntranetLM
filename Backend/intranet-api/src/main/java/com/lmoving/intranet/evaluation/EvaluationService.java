package com.lmoving.intranet.evaluation;

import com.lmoving.intranet.courses.CoursesRepository;
import com.lmoving.intranet.courses.courseAudience.CourseAudienceRepository;
import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.materials.CourseMaterialProgressService;
import com.lmoving.intranet.question.CourseQuestion;
import com.lmoving.intranet.question.Question;
import com.lmoving.intranet.question.QuestionOption;
import com.lmoving.intranet.question.CourseQuestionRepository;
import com.lmoving.intranet.question.QuestionOptionRepository;
import com.lmoving.intranet.evaluation.dto.EvaluationAnswerRequest;
import com.lmoving.intranet.evaluation.dto.EvaluationAttemptResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationQuestionOptionResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationQuestionResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationResultResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationStartResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationSubmitRequest;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class EvaluationService {

    private final CoursesRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseQuestionRepository courseQuestionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final EvaluationAttemptRepository evaluationAttemptRepository;
    private final CourseMaterialProgressService progressService;
    private final EvaluationAnswerRepository evaluationAnswerRepository;
    private final CourseAudienceRepository courseAudienceRepository;

    public EvaluationService(
        CoursesRepository courseRepository,
        UserRepository userRepository,
        CourseQuestionRepository courseQuestionRepository,
        QuestionOptionRepository questionOptionRepository,
        EvaluationAttemptRepository evaluationAttemptRepository,
        CourseMaterialProgressService progressService,
        EvaluationAnswerRepository evaluationAnswerRepository, 
        CourseAudienceRepository courseAudienceRepository
    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseQuestionRepository = courseQuestionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.evaluationAttemptRepository = evaluationAttemptRepository;
        this.progressService = progressService;
        this.evaluationAnswerRepository = evaluationAnswerRepository;
        this.courseAudienceRepository = courseAudienceRepository;
    }

    public EvaluationStartResponse startEvaluation(
        Long courseId,
        String username
    ) {

        Courses course = courseRepository.findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el curso con id: " + courseId
                )
            );

        if (!course.isActive()) {
            throw new IllegalArgumentException(
                "El curso no está activo"
            );
        }

        if (!progressService.hasCompletedRequiredMaterials(
                courseId,
                username
        )) {
            throw new IllegalStateException(
                "Debe completar todos los materiales obligatorios antes de realizar la evaluación"
            );
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el usuario: " + username
                )
            );

            if (!courseAudienceRepository.existsByCourseIdAndRole(
                    courseId,
                    user.getRole()
            )) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El curso no está asignado a tu rol"
                );
            }

        List<Question> questions = courseQuestionRepository
            .findByCourseId(courseId)
            .stream()
            .map(CourseQuestion::getQuestion)
            .filter(Question::isActive)
            .toList();

        int questionCount = course.getEvaluationQuestionCount();

        if (questions.size() < questionCount) {
            throw new IllegalStateException(
                "El curso no tiene suficientes preguntas activas para realizar la evaluación"
            );
        }

        List<Question> selectedQuestions =
            new java.util.ArrayList<>(questions);

        Collections.shuffle(selectedQuestions);

        selectedQuestions = selectedQuestions
            .subList(0, questionCount);

        EvaluationAttempt attempt = new EvaluationAttempt();
        attempt.setCourse(course);
        attempt.setUser(user);

        attempt = evaluationAttemptRepository.save(attempt);

        for (Question question : selectedQuestions) {

            EvaluationAnswer answer = new EvaluationAnswer();

            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(null);
            answer.setCorrect(false);

            evaluationAnswerRepository.save(answer);
        }

        List<EvaluationQuestionResponse> questionResponses =
            selectedQuestions.stream()
                .map(this::toEvaluationQuestionResponse)
                .toList();

        return new EvaluationStartResponse(
            attempt.getId(),
            course.getId(),
            course.getTitle(),
            course.getPassingScore(),
            questionCount,
            questionResponses
        );
    }

    private EvaluationQuestionResponse toEvaluationQuestionResponse(
        Question question
    ) {

        List<EvaluationQuestionOptionResponse> options =
            questionOptionRepository
                .findByQuestionId(question.getId())
                .stream()
                .map(this::toOptionResponse)
                .toList();

        return new EvaluationQuestionResponse(
            question.getId(),
            question.getQuestionText(),
            options
        );
    }

    private EvaluationQuestionOptionResponse toOptionResponse(
        QuestionOption option
    ) {
        return new EvaluationQuestionOptionResponse(
            option.getId(),
            option.getOptionText()
        );
    }

    public EvaluationResultResponse submitEvaluation(
            Long attemptId,
            String username,
            EvaluationSubmitRequest request
    ) {
        EvaluationAttempt attempt = evaluationAttemptRepository.findById(attemptId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el intento de evaluación con id: " + attemptId
                        )
                );
    
        // El intento debe pertenecer al usuario autenticado
        if (!attempt.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "El intento de evaluación no pertenece al usuario autenticado"
            );
        }
    
        // No permitir finalizar dos veces
        if (attempt.getCompletedAt() != null) {
            throw new IllegalStateException(
                    "La evaluación ya fue finalizada"
            );
        }
    
        // Obtener las preguntas que fueron asignadas REALMENTE a este intento
        List<EvaluationAnswer> evaluationAnswers =
                evaluationAnswerRepository.findByAttemptId(attemptId);
    
        if (evaluationAnswers.isEmpty()) {
            throw new IllegalStateException(
                    "El intento no tiene preguntas asignadas"
            );
        }
    
        // Evitar preguntas repetidas en el request
        long distinctQuestions = request.answers()
                .stream()
                .map(EvaluationAnswerRequest::questionId)
                .distinct()
                .count();
    
        if (distinctQuestions != request.answers().size()) {
            throw new IllegalArgumentException(
                    "No se puede enviar la misma pregunta más de una vez"
            );
        }
    
        // Deben enviarse exactamente las preguntas del intento
        if (request.answers().size() != evaluationAnswers.size()) {
            throw new IllegalArgumentException(
                    "Debe responder exactamente todas las preguntas de la evaluación"
            );
        }
    
        int correctAnswers = 0;
    
        for (EvaluationAnswerRequest requestAnswer : request.answers()) {
    
            EvaluationAnswer evaluationAnswer = evaluationAnswers.stream()
                    .filter(answer ->
                            answer.getQuestion().getId()
                                    .equals(requestAnswer.questionId())
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "La pregunta "
                                            + requestAnswer.questionId()
                                            + " no pertenece a este intento"
                            )
                    );
    
            QuestionOption selectedOption = null;
            boolean correct = false;
    
            if (requestAnswer.selectedOptionId() != null) {
    
                selectedOption = questionOptionRepository
                        .findById(requestAnswer.selectedOptionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe la opción seleccionada con id: "
                                                + requestAnswer.selectedOptionId()
                                )
                        );
    
                // La opción debe pertenecer a la pregunta
                if (!selectedOption.getQuestion().getId()
                        .equals(requestAnswer.questionId())) {
    
                    throw new IllegalArgumentException(
                            "La opción seleccionada no pertenece a la pregunta"
                    );
                }
    
                correct = selectedOption.isCorrect();
    
                if (correct) {
                    correctAnswers++;
                }
            }
    
            evaluationAnswer.setSelectedOption(selectedOption);
            evaluationAnswer.setCorrect(correct);
    
            evaluationAnswerRepository.save(evaluationAnswer);
        }
    
        int totalQuestions = evaluationAnswers.size();
    
        BigDecimal score = BigDecimal.valueOf(
                ((double) correctAnswers / totalQuestions) * 100
        ).setScale(2, RoundingMode.HALF_UP);
    
        boolean passed = score.compareTo(
                BigDecimal.valueOf(
                        attempt.getCourse().getPassingScore()
                )
        ) >= 0;
    
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setScore(score);
        attempt.setPassed(passed);
    
        evaluationAttemptRepository.save(attempt);
    
        return new EvaluationResultResponse(
                attempt.getId(),
                attempt.getCourse().getId(),
                attempt.getCourse().getTitle(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                totalQuestions,
                correctAnswers,
                score,
                attempt.getCourse().getPassingScore(),
                passed
        );
    }
    
    @Transactional
    public void cancelEvaluation(
            Long attemptId,
            String username
    ) {

        EvaluationAttempt attempt =
                evaluationAttemptRepository.findById(attemptId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el intento de evaluación con id: "
                                                + attemptId
                                )
                        );

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new IllegalStateException(
                    "El intento de evaluación no pertenece al usuario autenticado"
            );
        }

        if (attempt.getCompletedAt() != null) {
            throw new IllegalStateException(
                    "La evaluación ya fue finalizada"
            );
        }

        boolean hasAnsweredQuestions =
                evaluationAnswerRepository
                        .existsByAttemptIdAndSelectedOptionIsNotNull(attemptId);

        if (hasAnsweredQuestions) {
            throw new IllegalStateException(
                    "No se puede eliminar un intento que ya tiene respuestas"
            );
        }

        evaluationAttemptRepository.delete(attempt);
    }

    @Transactional(readOnly = true)
    public List<EvaluationAttemptResponse> getCompletedAttemptsByCourse(
            Long courseId
    ) {
    
        return evaluationAttemptRepository
                .findLatestCompletedAttemptByCourse(courseId)
                .stream()
                .map(attempt -> {
    
                    User user = attempt.getUser();
    
                    List<EvaluationAnswer> answers =
                            evaluationAnswerRepository.findByAttemptId(
                                    attempt.getId()
                            );

                    int totalQuestions = answers.size();

                    int correctAnswers = (int) answers
                            .stream()
                            .filter(EvaluationAnswer::isCorrect)
                            .count();
    
                    return new EvaluationAttemptResponse(
                            attempt.getId(),
                            user.getUsername(),
                            user.getFirstName() + " " + user.getLastName(),
                            user.getDocumentType().name(),
                            user.getDocumentNumber(),
                            attempt.getStartedAt(),
                            attempt.getCompletedAt(),
                            totalQuestions,
                            correctAnswers,
                            attempt.getScore(),
                            attempt.getCourse().getPassingScore(),
                            Boolean.TRUE.equals(attempt.getPassed())
                    );
                })
                .toList();
    }
}