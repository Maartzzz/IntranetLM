package com.lmoving.intranet.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, Long> {

    List<CourseQuestion> findByCourseId(Long courseId);

    List<CourseQuestion> findByQuestionId(Long questionId);

    boolean existsByCourseIdAndQuestionId(Long courseId, Long questionId);

    void deleteByCourseIdAndQuestionId(Long courseId, Long questionId);
}