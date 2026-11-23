-- Una operación puede tener como máximo una O/S del cliente.
-- La migración falla explícitamente si existen asociaciones duplicadas que deban reconciliarse.
DO $$
BEGIN
    IF EXISTS (
        SELECT operation_id
        FROM client_service_orders
        WHERE operation_id IS NOT NULL
        GROUP BY operation_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'No se puede aplicar V34: existen operaciones asociadas a más de una O/S del cliente';
    END IF;
END $$;

CREATE UNIQUE INDEX uq_client_service_orders_operation
    ON client_service_orders(operation_id)
    WHERE operation_id IS NOT NULL;
