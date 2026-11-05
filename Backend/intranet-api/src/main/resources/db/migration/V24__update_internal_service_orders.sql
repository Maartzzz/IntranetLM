-- V24: conservar el número externo de O/S cliente incluso sin relación FK.
ALTER TABLE internal_service_orders
    ADD COLUMN client_order_number VARCHAR(80);

-- Completa el número para las O/S internas ya vinculadas a una O/S cliente.
UPDATE internal_service_orders iso
SET client_order_number = cso.client_order_number
FROM client_service_orders cso
WHERE iso.client_service_order_id = cso.id
  AND iso.client_order_number IS NULL;
