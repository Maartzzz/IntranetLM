package com.lmoving.intranet.evaluation;

import com.lmoving.intranet.question.Question;
import com.lmoving.intranet.question.QuestionOption;
import jakarta.persistence.*;

@Entity
@Table(
    name = "evaluation_answers",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_evaluation_answers_question",
            columnNames = {"attempt_id", "question_id"}
        )
    }
)
public class EvaluationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private EvaluationAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(nullable = false)
    private boolean correct = false;

    public Long getId() {
        return id;
    }

    public EvaluationAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(EvaluationAttempt attempt) {
        this.attempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(QuestionOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}