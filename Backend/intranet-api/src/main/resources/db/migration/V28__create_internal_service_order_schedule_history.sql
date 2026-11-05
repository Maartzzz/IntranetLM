CREATE TABLE internal_service_order_schedule_history (
    id BIGSERIAL PRIMARY KEY,
    internal_service_order_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    previous_date DATE,
    new_date DATE,
    reason TEXT NOT NULL,
    actor_username VARCHAR(50) NOT NULL,
    actor_name VARCHAR(210) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_iosh_internal_service_order
        FOREIGN KEY (internal_service_order_id) REFERENCES internal_service_orders(id),
    CONSTRAINT ck_iosh_event_type CHECK (event_type IN
        ('PROGRAMACION_INICIAL', 'REPROGRAMACION', 'CANCELACION', 'REACTIVACION'))
);

CREATE INDEX idx_iosh_order_created
    ON internal_service_order_schedule_history (internal_service_order_id, created_at DESC, id DESC);
