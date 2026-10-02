ALTER TABLE course_material_progress
DROP CONSTRAINT fk_material_progress_user;

ALTER TABLE course_material_progress
DROP CONSTRAINT uq_material_progress_user_material;

ALTER TABLE course_material_progress
DROP COLUMN user_id;

ALTER TABLE course_material_progress
ADD COLUMN username VARCHAR(50) NOT NULL;

ALTER TABLE course_material_progress
ADD CONSTRAINT fk_material_progress_user
    FOREIGN KEY (username)
    REFERENCES users(username)
    ON DELETE CASCADE;

ALTER TABLE course_material_progress
ADD CONSTRAINT uq_material_progress_user_material
    UNIQUE (course_material_id, username);

CREATE INDEX idx_material_progress_username
    ON course_material_progress(username);