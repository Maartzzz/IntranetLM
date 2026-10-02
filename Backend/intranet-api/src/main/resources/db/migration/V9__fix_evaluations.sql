ALTER TABLE evaluation_answers
    DROP CONSTRAINT IF EXISTS fk_evaluation_answers_attempt;

ALTER TABLE evaluation_attempts
    DROP CONSTRAINT IF EXISTS fk_evaluation_attempts_user;

DROP INDEX IF EXISTS idx_evaluation_attempts_user_id;

ALTER TABLE evaluation_attempts
    DROP COLUMN user_id;

ALTER TABLE evaluation_attempts
    ADD COLUMN username VARCHAR(50) NOT NULL;

ALTER TABLE evaluation_attempts
    ADD CONSTRAINT fk_evaluation_attempts_user
        FOREIGN KEY (username)
        REFERENCES users(username)
        ON DELETE CASCADE;

CREATE INDEX idx_evaluation_attempts_username
    ON evaluation_attempts(username);