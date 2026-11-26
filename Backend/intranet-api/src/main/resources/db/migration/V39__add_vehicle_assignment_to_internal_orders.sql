ALTER TABLE internal_service_orders ADD COLUMN IF NOT EXISTS vehicle_id BIGINT REFERENCES vehicles(id);
ALTER TABLE internal_service_orders ADD COLUMN IF NOT EXISTS assigned_vehicle_plate_snapshot VARCHAR(20);
ALTER TABLE internal_service_orders ADD COLUMN IF NOT EXISTS assigned_vehicle_type_snapshot VARCHAR(20);
ALTER TABLE internal_service_orders ADD COLUMN IF NOT EXISTS assigned_trailer_plate_snapshot VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_internal_service_orders_vehicle_id ON internal_service_orders(vehicle_id);
