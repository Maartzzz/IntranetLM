CREATE TABLE course_audiences (
    id BIGSERIAL PRIMARY KEY,

    course_id BIGINT NOT NULL,

    role VARCHAR(30) NOT NULL,

    CONSTRAINT fk_course_audiences_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_course_audience
        UNIQUE (course_id, role)
);