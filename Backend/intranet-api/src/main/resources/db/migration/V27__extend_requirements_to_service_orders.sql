-- V27: Permite requisitos independientes para Operación, O/S cliente y O/S interna.
-- Conserva todos los requisitos existentes asociados a Operaciones.

ALTER TABLE operation_requirements
    ALTER COLUMN operation_id DROP NOT NULL,
    ADD COLUMN client_service_order_id BIGINT,
    ADD COLUMN internal_service_order_id BIGINT;

ALTER TABLE operation_requirements
    ADD CONSTRAINT fk_operation_requirements_client_order
        FOREIGN KEY (client_service_order_id) REFERENCES client_service_orders(id),
    ADD CONSTRAINT fk_operation_requirements_internal_order
        FOREIGN KEY (internal_service_order_id) REFERENCES internal_service_orders(id),
    ADD CONSTRAINT ck_operation_requirements_single_owner CHECK (
        num_nonnulls(operation_id, client_service_order_id, internal_service_order_id) = 1
    );

-- Índices únicos independientes por propietario para requisitos del catálogo.
CREATE UNIQUE INDEX uq_client_order_requirement_catalog
    ON operation_requirements(client_service_order_id, requirement_catalog_id)
    WHERE client_service_order_id IS NOT NULL AND requirement_catalog_id IS NOT NULL;

CREATE UNIQUE INDEX uq_client_order_requirement_custom
    ON operation_requirements(client_service_order_id, lower(trim(custom_name)))
    WHERE client_service_order_id IS NOT NULL AND requirement_catalog_id IS NULL;

CREATE UNIQUE INDEX uq_internal_order_requirement_catalog
    ON operation_requirements(internal_service_order_id, requirement_catalog_id)
    WHERE internal_service_order_id IS NOT NULL AND requirement_catalog_id IS NOT NULL;

CREATE UNIQUE INDEX uq_internal_order_requirement_custom
    ON operation_requirements(internal_service_order_id, lower(trim(custom_name)))
    WHERE internal_service_order_id IS NOT NULL AND requirement_catalog_id IS NULL;

CREATE INDEX idx_operation_requirements_client_order
    ON operation_requirements(client_service_order_id)
    WHERE client_service_order_id IS NOT NULL;

CREATE INDEX idx_operation_requirements_internal_order
    ON operation_requirements(internal_service_order_id)
    WHERE internal_service_order_id IS NOT NULL;
