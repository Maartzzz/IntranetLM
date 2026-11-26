CREATE TABLE operation_import_batches (
    id BIGSERIAL PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'PREVIEW',
    total_rows INTEGER NOT NULL DEFAULT 0,
    created_rows INTEGER NOT NULL DEFAULT 0,
    skipped_rows INTEGER NOT NULL DEFAULT 0,
    mapping_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_operation_import_batches_created_at
    ON operation_import_batches (created_at DESC);

CREATE TABLE operation_import_files (
    batch_id BIGINT PRIMARY KEY REFERENCES operation_import_batches(id) ON DELETE CASCADE,
    original_file BYTEA NOT NULL
);

CREATE TABLE operation_import_profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    mapping_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
