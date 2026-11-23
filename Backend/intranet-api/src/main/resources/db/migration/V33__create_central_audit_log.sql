-- Registro centralizado e inmutable de auditoría.
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_username VARCHAR(50) NOT NULL,
    actor_name VARCHAR(201) NOT NULL,
    module VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id BIGINT,
    entity_key VARCHAR(100),
    action VARCHAR(40) NOT NULL,
    description VARCHAR(500) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    legacy_activity_id BIGINT UNIQUE
);

CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC, id DESC);
CREATE INDEX idx_audit_log_module_created ON audit_log(module, created_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id, created_at DESC);

-- Copia los eventos históricos sin alterar operation_activity ni sus identificadores.
INSERT INTO audit_log (
    actor_username, actor_name, module, entity_type, entity_id, entity_key,
    action, description, old_value, new_value, created_at, legacy_activity_id
)
SELECT actor_username, actor_name, 'OPERACIONES', entity_type, entity_id, NULL,
       action, description, old_value, new_value, created_at, id
FROM operation_activity
ON CONFLICT (legacy_activity_id) DO NOTHING;
