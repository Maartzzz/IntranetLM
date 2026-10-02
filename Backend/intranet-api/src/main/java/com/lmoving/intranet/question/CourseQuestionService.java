package com.lmoving.intranet.question;

import com.lmoving.intranet.courses.Courses;
import com.lmoving.intranet.courses.CoursesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseQuestionService {

    private final CourseQuestionRepository courseQuestionRepository;
    private final QuestionRepository questionRepository;
    private final CoursesRepository courseRepository;

    public CourseQuestionService(
        CourseQuestionRepository courseQuestionRepository,
        QuestionRepository questionRepository,
        CoursesRepository courseRepository
    ) {
        this.courseQuestionRepository = courseQuestionRepository;
        this.questionRepository = questionRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsByCourse(Long courseId) {

        findCourse(courseId);

        return courseQuestionRepository.findByCourseId(courseId)
            .stream()
            .map(CourseQuestion::getQuestion)
            .toList();
    }

    public void assignQuestion(Long courseId, Long questionId) {

        Courses course = findCourse(courseId);
        Question question = findQuestion(questionId);

        if (courseQuestionRepository.existsByCourseIdAndQuestionId(
                courseId,
                questionId
        )) {
            throw new IllegalArgumentException(
                "La pregunta ya está asociada al curso"
            );
        }

        CourseQuestion courseQuestion = new CourseQuestion();
        courseQuestion.setCourse(course);
        courseQuestion.setQuestion(question);

        courseQuestionRepository.save(courseQuestion);
    }

    public void removeQuestion(Long courseId, Long questionId) {

        findCourse(courseId);
        findQuestion(questionId);

        if (!courseQuestionRepository.existsByCourseIdAndQuestionId(
                courseId,
                questionId
        )) {
            throw new IllegalArgumentException(
                "La pregunta no está asociada al curso"
            );
        }

        courseQuestionRepository.deleteByCourseIdAndQuestionId(
            courseId,
            questionId
        );
    }

    private Courses findCourse(Long courseId) {
        return courseRepository.findById(courseId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el curso con id: " + courseId
                )
            );
    }

    private Question findQuestion(Long questionId) {
        return questionRepository.findById(questionId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe la pregunta con id: " + questionId
                )
            );
    }
}