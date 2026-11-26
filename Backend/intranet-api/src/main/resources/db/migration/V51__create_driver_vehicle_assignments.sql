CREATE TABLE driver_vehicle_assignments (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP,
    reason VARCHAR(40) NOT NULL DEFAULT 'ASIGNACION',
    observations TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_driver_vehicle_assignment_dates CHECK (end_at IS NULL OR end_at >= start_at)
);

CREATE INDEX ix_driver_vehicle_assignments_vehicle_active
    ON driver_vehicle_assignments(vehicle_id) WHERE end_at IS NULL;
CREATE INDEX ix_driver_vehicle_assignments_driver_active
    ON driver_vehicle_assignments(driver_id) WHERE end_at IS NULL;
CREATE INDEX ix_driver_vehicle_assignments_history
    ON driver_vehicle_assignments(start_at DESC, id DESC);
