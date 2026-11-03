CREATE TABLE evaluation_rounds (
    id BIGSERIAL PRIMARY KEY,

    course_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    round_number INTEGER NOT NULL,

    authorized_by VARCHAR(50),
    authorized_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_evaluation_rounds_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id),

    CONSTRAINT fk_evaluation_rounds_user
        FOREIGN KEY (username)
        REFERENCES users(username)
        ON DELETE CASCADE,

    CONSTRAINT fk_evaluation_rounds_authorizer
        FOREIGN KEY (authorized_by)
        REFERENCES users(username),

    CONSTRAINT uq_evaluation_rounds_user_course_number
        UNIQUE (course_id, username, round_number),

    CONSTRAINT ck_evaluation_rounds_number
        CHECK (round_number >= 1)
);

ALTER TABLE evaluation_attempts
    ADD COLUMN round_id BIGINT;

ALTER TABLE evaluation_attempts
    ADD CONSTRAINT fk_evaluation_attempts_round
        FOREIGN KEY (round_id)
        REFERENCES evaluation_rounds(id);

CREATE INDEX idx_evaluation_rounds_user_course
    ON evaluation_rounds(username, course_id, round_number);

CREATE INDEX idx_evaluation_attempts_round
    ON evaluation_attempts(round_id);
