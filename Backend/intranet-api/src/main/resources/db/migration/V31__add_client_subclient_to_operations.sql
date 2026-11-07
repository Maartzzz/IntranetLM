-- V31: Asociar opcionalmente una operación con una relación cliente-subcliente.
-- No modifica registros existentes: las operaciones anteriores quedan sin subcliente.

ALTER TABLE operations
    ADD COLUMN client_subclient_id BIGINT;

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_client_subclient
        FOREIGN KEY (client_subclient_id) REFERENCES client_subclients(id);

CREATE INDEX idx_operations_client_subclient_id
    ON operations(client_subclient_id);

