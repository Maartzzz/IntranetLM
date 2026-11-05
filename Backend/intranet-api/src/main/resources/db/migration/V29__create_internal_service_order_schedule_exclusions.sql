CREATE TABLE internal_service_order_schedule_exclusions (
    id BIGSERIAL PRIMARY KEY,
    internal_service_order_id BIGINT NOT NULL,
    excluded_date DATE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ios_exclusion_order
        FOREIGN KEY (internal_service_order_id)
        REFERENCES internal_service_orders(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_ios_exclusion_order_date
        UNIQUE (internal_service_order_id, excluded_date)
);

CREATE INDEX idx_ios_exclusion_date
    ON internal_service_order_schedule_exclusions (excluded_date);