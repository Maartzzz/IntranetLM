
CREATE TABLE comunicacion_interna (
    id BIGSERIAL PRIMARY KEY,

    username VARCHAR(50) NOT NULL,

    sender_name VARCHAR(201) NOT NULL,

    type VARCHAR(20) NOT NULL
        CHECK (type IN ('SUGERENCIA', 'QUEJA', 'OTRO')),

    subject VARCHAR(150) NOT NULL,

    description TEXT NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    email_status VARCHAR(20) NOT NULL
        DEFAULT 'PENDIENTE'
        CHECK (email_status IN ('PENDIENTE', 'ENVIADO', 'ERROR')),

    email_error VARCHAR(500),

    sent_at TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT fk_comunicacion_interna_user
        FOREIGN KEY (username)
        REFERENCES users(username)
);

CREATE INDEX idx_comunicacion_interna_username
    ON comunicacion_interna(username);

CREATE INDEX idx_comunicacion_interna_created_at
    ON comunicacion_interna(created_at DESC);

CREATE INDEX idx_comunicacion_interna_email_status
    ON comunicacion_interna(email_status);
