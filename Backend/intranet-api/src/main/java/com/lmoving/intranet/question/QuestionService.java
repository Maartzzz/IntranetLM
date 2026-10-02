package com.lmoving.intranet.question;

import com.lmoving.intranet.question.dto.QuestionOptionRequest;
import com.lmoving.intranet.question.dto.QuestionOptionResponse;
import com.lmoving.intranet.question.dto.QuestionRequest;
import com.lmoving.intranet.question.dto.QuestionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    public QuestionService(
        QuestionRepository questionRepository,
        QuestionOptionRepository questionOptionRepository
    ) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getAll() {
        return questionRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(Long id) {
        Question question = findQuestion(id);
        return toResponse(question);
    }

    public QuestionResponse create(QuestionRequest request) {
        validateCorrectOption(request.options());

        Question question = new Question();
        question.setQuestionText(request.questionText());
        question.setActive(request.active());

        question = questionRepository.save(question);

        saveOptions(question, request.options());

        return toResponse(question);
    }

    public QuestionResponse update(Long id, QuestionRequest request) {
        validateCorrectOption(request.options());

        Question question = findQuestion(id);

        question.setQuestionText(request.questionText());
        question.setActive(request.active());

        questionOptionRepository.deleteAll(
            questionOptionRepository.findByQuestionId(id)
        );

        saveOptions(question, request.options());

        return toResponse(question);
    }

    public QuestionResponse toggleActive(Long id) {
        Question question = findQuestion(id);

        question.setActive(!question.isActive());

        return toResponse(questionRepository.save(question));
    }

    public void delete(Long id) {
        Question question = findQuestion(id);
        questionRepository.delete(question);
    }

    private void validateCorrectOption(List<QuestionOptionRequest> options) {

        long correctCount = options.stream()
            .filter(QuestionOptionRequest::correct)
            .count();

        if (correctCount != 1) {
            throw new IllegalArgumentException(
                "La pregunta debe tener exactamente una opción correcta"
            );
        }
    }

    private void saveOptions(
        Question question,
        List<QuestionOptionRequest> options
    ) {
        List<QuestionOption> entities = options.stream()
            .map(optionRequest -> {
                QuestionOption option = new QuestionOption();

                option.setQuestion(question);
                option.setOptionText(optionRequest.optionText());
                option.setCorrect(optionRequest.correct());

                return option;
            })
            .toList();

        questionOptionRepository.saveAll(entities);
    }

    private Question findQuestion(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe la pregunta con id: " + id
                )
            );
    }

    private QuestionResponse toResponse(Question question) {

        List<QuestionOptionResponse> options =
            questionOptionRepository.findByQuestionId(question.getId())
                .stream()
                .map(option -> new QuestionOptionResponse(
                    option.getId(),
                    option.getOptionText(),
                    option.isCorrect()
                ))
                .toList();

        return new QuestionResponse(
            question.getId(),
            question.getQuestionText(),
            question.isActive(),
            options,
            question.getCreatedAt(),
            question.getUpdatedAt()
        );
    }
}