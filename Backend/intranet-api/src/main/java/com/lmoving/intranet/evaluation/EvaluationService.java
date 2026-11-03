package com.lmoving.intranet.evaluation;

import com.lmoving.intranet.courses.CoursesRepository;
import com.lmoving.intranet.courses.courseAudience.CourseAudience;
import com.lmoving.intranet.courses.courseAudience.CourseAudienceRepository;
import com.lmoving.intranet.courses.courseAudience.CourseAudienceService;
import com.lmoving.intranet.courses.dto.CourseResponse;
import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.materials.CourseMaterialProgressService;
import com.lmoving.intranet.question.CourseQuestion;
import com.lmoving.intranet.question.Question;
import com.lmoving.intranet.question.QuestionOption;
import com.lmoving.intranet.question.CourseQuestionRepository;
import com.lmoving.intranet.question.QuestionOptionRepository;
import com.lmoving.intranet.evaluation.dto.EvaluationAnswerProgressRequest;
import com.lmoving.intranet.evaluation.dto.EvaluationAnswerRequest;
import com.lmoving.intranet.evaluation.dto.EvaluationAttemptResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationQuestionOptionResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationQuestionResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationResultResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationResumeResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationSavedAnswerResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationStartResponse;
import com.lmoving.intranet.evaluation.dto.EvaluationSubmitRequest;
import com.lmoving.intranet.evaluation.dto.ExhaustedEvaluationResponse;
import com.lmoving.intranet.evaluation.dto.MyEvaluationResponse;
import com.lmoving.intranet.evaluation.dto.PendingEvaluationResponse;
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
    private final EvaluationRoundRepository evaluationRoundRepository;
    private final CourseAudienceService courseAudienceService;

    public EvaluationService(
        CoursesRepository courseRepository,
        UserRepository userRepository,
        CourseQuestionRepository courseQuestionRepository,
        QuestionOptionRepository questionOptionRepository,
        EvaluationAttemptRepository evaluationAttemptRepository,
        CourseMaterialProgressService progressService,
        EvaluationAnswerRepository evaluationAnswerRepository, 
        CourseAudienceRepository courseAudienceRepository, 
        EvaluationRoundRepository evaluationRoundRepository, CourseAudienceService courseAudienceService
    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseQuestionRepository = courseQuestionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.evaluationAttemptRepository = evaluationAttemptRepository;
        this.progressService = progressService;
        this.evaluationAnswerRepository = evaluationAnswerRepository;
        this.courseAudienceRepository = courseAudienceRepository;
        this.evaluationRoundRepository = evaluationRoundRepository;
        this.courseAudienceService = courseAudienceService;
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
                
        EvaluationRound round = evaluationRoundRepository
            .findFirstByCourse_IdAndUser_UsernameOrderByRoundNumberDesc(
                courseId,
                username
            )
            .orElse(null);

        if (round == null) {
            round = new EvaluationRound();
            round.setCourse(course);
            round.setUser(user);
            round.setRoundNumber(1);
            round = evaluationRoundRepository.save(round);
        }

        long completedAttempts =
            evaluationAttemptRepository
                .countByRound_IdAndCompletedAtIsNotNull(round.getId());

        if (evaluationAttemptRepository
                .existsByRound_IdAndCompletedAtIsNull(round.getId())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya tienes una evaluación en curso para este curso"
            );
        }

        if (evaluationAttemptRepository
                .existsByRound_IdAndPassedTrueAndCompletedAtIsNotNull(
                    round.getId()
                )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya aprobaste la evaluación de este curso"
            );
        }

        if (completedAttempts >= 3) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Agotaste los 3 intentos. Solicita una rehabilitación a ADMIN o SUPERVISOR"
            );
        }

        EvaluationAttempt attempt = new EvaluationAttempt();
        attempt.setCourse(course);
        attempt.setUser(user);
        attempt.setRound(round);

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

    
    public EvaluationRound rehabilitateEvaluation(
            Long courseId,
            String targetUsername,
            String authorizerUsername
    ) {
        User authorizer = userRepository.findByUsername(authorizerUsername)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el usuario autorizador"
                )
            );

        if (authorizer.getRole() != com.lmoving.intranet.user.Role.ADMIN
                && authorizer.getRole()
                    != com.lmoving.intranet.user.Role.SUPERVISOR) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo ADMIN o SUPERVISOR pueden rehabilitar evaluaciones"
            );
        }

        EvaluationRound currentRound = evaluationRoundRepository
            .findFirstByCourse_IdAndUser_UsernameOrderByRoundNumberDesc(
                courseId,
                targetUsername
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El trabajador todavía no tiene una ronda de evaluación"
                )
            );

        long completedAttempts =
            evaluationAttemptRepository
                .countByRound_IdAndCompletedAtIsNotNull(
                    currentRound.getId()
                );

        if (evaluationAttemptRepository
                .existsByRound_IdAndCompletedAtIsNull(
                    currentRound.getId()
                )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El trabajador tiene una evaluación en curso"
            );
        }

        if (completedAttempts < 3) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El trabajador todavía no ha agotado los 3 intentos"
            );
        }

        if (evaluationAttemptRepository
                .existsByRound_IdAndPassedTrueAndCompletedAtIsNotNull(
                    currentRound.getId()
                )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El trabajador ya aprobó esta ronda"
            );
        }

        EvaluationRound newRound = new EvaluationRound();
        newRound.setCourse(currentRound.getCourse());
        newRound.setUser(currentRound.getUser());
        newRound.setRoundNumber(currentRound.getRoundNumber() + 1);
        newRound.setAuthorizedBy(authorizer);
        newRound.setAuthorizedAt(LocalDateTime.now());

        return evaluationRoundRepository.save(newRound);
    }

    @Transactional(readOnly = true)
    public List<ExhaustedEvaluationResponse> getExhaustedEvaluations() {
        return evaluationRoundRepository.findExhaustedCurrentRounds()
                .stream()
                .map(round -> {
                    User user = round.getUser();
    
                    long completedAttempts =
                            evaluationAttemptRepository
                                    .countByRound_IdAndCompletedAtIsNotNull(
                                            round.getId()
                                    );
    
                    LocalDateTime lastAttemptAt =
                            evaluationAttemptRepository
                                    .findFirstByRound_IdAndCompletedAtIsNotNullOrderByCompletedAtDesc(
                                            round.getId()
                                    )
                                    .map(EvaluationAttempt::getCompletedAt)
                                    .orElse(null);
    
                    return new ExhaustedEvaluationResponse(
                            round.getCourse().getId(),
                            round.getCourse().getTitle(),
                            user.getUsername(),
                            user.getFirstName() + " " + user.getLastName(),
                            user.getDocumentType().name(),
                            user.getDocumentNumber(),
                            round.getRoundNumber(),
                            completedAttempts,
                            lastAttemptAt
                    );
                })
                .toList();
    }

    
    @Transactional(readOnly = true)
    public List<PendingEvaluationResponse> getPendingEvaluations(
            String username
    ) {
        return evaluationAttemptRepository
                .findByCompletedAtIsNullOrderByStartedAtAsc()
                .stream()
                .filter(attempt ->
                        attempt.getUser().getUsername().equals(username)
                )
                .map(attempt -> {
                    User user = attempt.getUser();
    
                    return new PendingEvaluationResponse(
                            attempt.getId(),
                            attempt.getCourse().getId(),
                            attempt.getCourse().getTitle(),
                            user.getUsername(),
                            user.getFirstName() + " " + user.getLastName(),
                            user.getDocumentType().name(),
                            user.getDocumentNumber(),
                            attempt.getRound().getRoundNumber(),
                            attempt.getStartedAt()
                    );
                })
                .toList();
    }
    
    @Transactional(readOnly = true)
    public EvaluationResumeResponse resumeEvaluation(
            Long attemptId,
            String username
    ) {
        EvaluationAttempt attempt = evaluationAttemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el intento de evaluación"
                ));
    
        if (!attempt.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El intento no pertenece al usuario autenticado"
            );
        }
    
        if (attempt.getCompletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La evaluación ya fue finalizada"
            );
        }
    
        List<EvaluationAnswer> answers =
                evaluationAnswerRepository.findByAttemptId(attemptId);
    
        if (answers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El intento no tiene preguntas asignadas"
            );
        }
    
        List<EvaluationQuestionResponse> questions = answers.stream()
                .map(answer ->
                        toEvaluationQuestionResponse(answer.getQuestion())
                )
                .toList();
    
        List<EvaluationSavedAnswerResponse> savedAnswers = answers.stream()
                .map(answer -> new EvaluationSavedAnswerResponse(
                        answer.getQuestion().getId(),
                        answer.getSelectedOption() != null
                                ? answer.getSelectedOption().getId()
                                : null
                ))
                .toList();
    
        return new EvaluationResumeResponse(
                attempt.getId(),
                attempt.getCourse().getId(),
                attempt.getCourse().getTitle(),
                attempt.getCourse().getPassingScore(),
                questions.size(),
                questions,
                savedAnswers
        );
    }
    
    @Transactional
    public void saveAnswerProgress(
            Long attemptId,
            String username,
            EvaluationAnswerProgressRequest request
    ) {
        EvaluationAttempt attempt = evaluationAttemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el intento de evaluación"
                ));
    
        if (!attempt.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El intento no pertenece al usuario autenticado"
            );
        }
    
        if (attempt.getCompletedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La evaluación ya fue finalizada"
            );
        }
    
        EvaluationAnswer answer = evaluationAnswerRepository
                .findByAttemptId(attemptId)
                .stream()
                .filter(item -> item.getQuestion().getId()
                        .equals(request.questionId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La pregunta no pertenece a este intento"
                ));
    
        QuestionOption selectedOption = null;
    
        if (request.selectedOptionId() != null) {
            selectedOption = questionOptionRepository
                    .findById(request.selectedOptionId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La opción seleccionada no existe"
                    ));
    
            if (!selectedOption.getQuestion().getId()
                    .equals(request.questionId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La opción no pertenece a la pregunta"
                );
            }
        }
    
        answer.setSelectedOption(selectedOption);
        evaluationAnswerRepository.save(answer);
    }
    
    @Transactional(readOnly = true)
    public List<MyEvaluationResponse> getMyEvaluations(String username) {

        return evaluationAttemptRepository
                .findByUsername_UsernameOrderByStartedAtDesc(username)
                .stream()
                .map(attempt -> new MyEvaluationResponse(
                        attempt.getId(),
                        attempt.getCourse().getId(),
                        attempt.getCourse().getTitle(),
                        attempt.getStartedAt(),
                        attempt.getCompletedAt(),
                        attempt.getScore(),
                        attempt.getCourse().getPassingScore(),
                        attempt.getPassed()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAvailableEvaluationCourses(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el usuario: " + username
                ));

        List<CourseResponse> assignedCourses =
                courseAudienceService.getCoursesForRole(user.getRole());

        return assignedCourses.stream()
                .filter(course -> {
                    EvaluationRound round = evaluationRoundRepository
                            .findFirstByCourse_IdAndUser_UsernameOrderByRoundNumberDesc(
                                    course.id(),
                                    username
                            )
                            .orElse(null);

                    if (round == null) {
                        return true;
                    }

                    boolean pending = evaluationAttemptRepository
                            .existsByRound_IdAndCompletedAtIsNull(round.getId());

                    boolean passed = evaluationAttemptRepository
                            .existsByRound_IdAndPassedTrueAndCompletedAtIsNotNull(
                                    round.getId()
                            );

                    long completed = evaluationAttemptRepository
                            .countByRound_IdAndCompletedAtIsNotNull(round.getId());

                    boolean available = !pending && !passed && completed < 3;

                    return available;
                })
                .toList();
    }
}