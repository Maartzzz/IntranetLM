package com.lmoving.intranet.question;

import com.lmoving.intranet.courses.Courses;
import jakarta.persistence.*;

@Entity
@Table(
    name = "course_questions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_course_questions",
            columnNames = {"course_id", "question_id"}
        )
    }
)
public class CourseQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public Long getId() {
        return id;
    }

    public Courses getCourse() {
        return course;
    }

    public void setCourse(Courses course) {
        this.course = course;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}