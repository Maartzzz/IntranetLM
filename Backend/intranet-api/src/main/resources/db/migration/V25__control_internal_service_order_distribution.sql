-- V25: estado de cancelacion para O/S internas y control de distribucion.
-- Las cantidades distribuidas se calculan desde las O/S internas vigentes;
-- no se almacenan contadores redundantes que puedan quedar desincronizados.
INSERT INTO statuses (process_type_id, name, description, color, active)
SELECT p.id, 'Cancelada', 'La O/S interna fue cancelada y libera su distribución.', '#DC2626', TRUE
FROM process_types p
WHERE p.code = 'OS_INTERNA'
ON CONFLICT (process_type_id, name) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_internal_service_orders_distribution
    ON internal_service_orders(client_service_order_id, status_id);
