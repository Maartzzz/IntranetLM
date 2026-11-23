ALTER TABLE client_service_orders ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE internal_service_orders ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX idx_client_service_orders_active ON client_service_orders(active);
CREATE INDEX idx_internal_service_orders_active ON internal_service_orders(active);
