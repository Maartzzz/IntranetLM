-- ============================================================
-- V10 - Restaurar FK de evaluation_answers con CASCADE
-- ============================================================

ALTER TABLE evaluation_answers
    ADD CONSTRAINT fk_evaluation_answers_attempt
        FOREIGN KEY (attempt_id)
        REFERENCES evaluation_attempts(id)
        ON DELETE CASCADE;