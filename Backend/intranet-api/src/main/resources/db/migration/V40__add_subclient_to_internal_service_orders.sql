ALTER TABLE internal_service_orders ADD COLUMN IF NOT EXISTS client_subclient_id BIGINT REFERENCES client_subclients(id);
CREATE INDEX IF NOT EXISTS idx_internal_service_orders_client_subclient_id ON internal_service_orders(client_subclient_id);
