CREATE TABLE internal_service_order_schedule_versions (
 id BIGSERIAL PRIMARY KEY,
 version_number INTEGER NOT NULL,
 start_date DATE NOT NULL,
 end_date DATE NOT NULL,
 snapshot JSONB NOT NULL,
 created_by_username VARCHAR(50) NOT NULL,
 created_by_name VARCHAR(210) NOT NULL,
 created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_ios_schedule_version_dates CHECK (end_date >= start_date),
 CONSTRAINT chk_ios_schedule_version_number CHECK (version_number > 0),
 CONSTRAINT uq_ios_schedule_version_period UNIQUE (start_date, end_date, version_number)
);
CREATE INDEX idx_ios_schedule_versions_period
 ON internal_service_order_schedule_versions (start_date, end_date, version_number DESC);
