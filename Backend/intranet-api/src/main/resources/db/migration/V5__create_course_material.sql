CREATE TABLE course_materials (
    id BIGSERIAL PRIMARY KEY,

    course_id BIGINT NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT,

    type VARCHAR(30) NOT NULL,

    url TEXT NOT NULL,

    required BOOLEAN NOT NULL DEFAULT FALSE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_course_material_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_course_material_type
        CHECK (type IN ('VIDEO', 'PPTX', 'DOCUMENT'))
);


CREATE INDEX idx_course_materials_course_id
    ON course_materials(course_id);


CREATE TABLE course_material_progress (
    id BIGSERIAL PRIMARY KEY,

    course_material_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    completed_at TIMESTAMP,

    progress_percent INTEGER NOT NULL DEFAULT 0,

    last_position BIGINT,

    CONSTRAINT fk_material_progress_material
        FOREIGN KEY (course_material_id)
        REFERENCES course_materials(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_material_progress_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_material_progress_percent
        CHECK (progress_percent BETWEEN 0 AND 100),

    CONSTRAINT uq_material_progress_user_material
        UNIQUE (course_material_id, user_id)
);


CREATE INDEX idx_material_progress_material
    ON course_material_progress(course_material_id);


CREATE INDEX idx_material_progress_user
    ON course_material_progress(user_id);