ALTER TABLE operations
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN archived_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN archive_reason VARCHAR(30),
    ADD COLUMN status_changed_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE operations o
SET status_changed_at = COALESCE(o.updated_at, o.created_at, CURRENT_TIMESTAMP)
FROM statuses s
WHERE o.status_id = s.id
  AND lower(trim(s.name)) IN ('finalizada', 'cancelada');

CREATE INDEX idx_operations_archive_status
    ON operations(archived, status_id, status_changed_at);
