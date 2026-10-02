-- ============================================================
-- V8 - Evaluaciones de cursos
-- ============================================================

-- ------------------------------------------------------------
-- 1. Configuración de evaluación en cursos
-- ------------------------------------------------------------

ALTER TABLE courses
ADD COLUMN evaluation_question_count INTEGER NOT NULL DEFAULT 10;

ALTER TABLE courses
ADD CONSTRAINT chk_courses_evaluation_question_count
CHECK (evaluation_question_count > 0);


-- ------------------------------------------------------------
-- 2. Banco de preguntas
-- ------------------------------------------------------------

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    question_text TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ------------------------------------------------------------
-- 3. Opciones de respuesta
-- ------------------------------------------------------------

CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_question_options_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE
);


-- ------------------------------------------------------------
-- 4. Preguntas asociadas a cursos
-- ------------------------------------------------------------

CREATE TABLE course_questions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,

    CONSTRAINT fk_course_questions_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_course_questions_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_course_questions
        UNIQUE (course_id, question_id)
);


-- ------------------------------------------------------------
-- 5. Intentos de evaluación
-- ------------------------------------------------------------

CREATE TABLE evaluation_attempts (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,

    score NUMERIC(5,2),
    passed BOOLEAN,

    CONSTRAINT fk_evaluation_attempts_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_evaluation_attempts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_evaluation_attempts_score
        CHECK (score IS NULL OR (score >= 0 AND score <= 100))
);


-- ------------------------------------------------------------
-- 6. Respuestas de cada intento
-- ------------------------------------------------------------

CREATE TABLE evaluation_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT,
    correct BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_evaluation_answers_attempt
        FOREIGN KEY (attempt_id)
        REFERENCES evaluation_attempts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_evaluation_answers_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_evaluation_answers_option
        FOREIGN KEY (selected_option_id)
        REFERENCES question_options(id)
        ON DELETE SET NULL,

    CONSTRAINT uq_evaluation_answers_question
        UNIQUE (attempt_id, question_id)
);


-- ------------------------------------------------------------
-- 7. Índices
-- ------------------------------------------------------------

CREATE INDEX idx_question_options_question_id
    ON question_options(question_id);

CREATE INDEX idx_course_questions_course_id
    ON course_questions(course_id);

CREATE INDEX idx_course_questions_question_id
    ON course_questions(question_id);

CREATE INDEX idx_evaluation_attempts_course_id
    ON evaluation_attempts(course_id);

CREATE INDEX idx_evaluation_attempts_user_id
    ON evaluation_attempts(user_id);

CREATE INDEX idx_evaluation_answers_attempt_id
    ON evaluation_answers(attempt_id);

CREATE INDEX idx_evaluation_answers_question_id
    ON evaluation_answers(question_id);