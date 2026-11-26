-- Etapa 4: autorización centralizada para comunicaciones y certificados.
-- Se conserva la disponibilidad previa de envío de comunicaciones para usuarios
-- autenticados, la consulta de certificados para ADMIN/SUPERVISOR y la descarga
-- de certificados propios para cualquier usuario autenticado.

INSERT INTO permission_modules (code, name, description, display_order)
VALUES
    ('COMMUNICATIONS', 'Comunicaciones', 'Envío de comunicaciones desde la intranet', 170),
    ('CERTIFICATES', 'Certificados', 'Consulta y descarga de certificados de capacitación', 180)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (module_id, code, name, description)
SELECT m.id, x.code, x.name, x.description
FROM permission_modules m
JOIN (VALUES
    ('COMMUNICATIONS', 'COMMUNICATIONS.SEND', 'Enviar comunicación', 'Enviar una comunicación desde la intranet'),
    ('CERTIFICATES', 'CERTIFICATES.VIEW', 'Ver certificados', 'Consultar el listado de certificados emitidos'),
    ('CERTIFICATES', 'CERTIFICATES.DOWNLOAD_OWN', 'Descargar certificado propio', 'Descargar el certificado de una evaluación aprobada propia'),
    ('CERTIFICATES', 'CERTIFICATES.DOWNLOAD_ANY', 'Descargar cualquier certificado', 'Descargar el certificado de cualquier usuario')
) AS x(module_code, code, name, description)
    ON x.module_code = m.code
ON CONFLICT (code) DO NOTHING;

-- El envío de comunicaciones estaba disponible para cualquier usuario autenticado.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'COMMUNICATIONS.SEND'
WHERE r.code IN ('ADMIN', 'SUPERVISOR', 'CHOFER', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- La descarga del certificado propio estaba disponible para cualquier usuario
-- autenticado y el listado para ADMIN/SUPERVISOR.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'CERTIFICATES.DOWNLOAD_OWN'
WHERE r.code IN ('ADMIN', 'SUPERVISOR', 'CHOFER', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'CERTIFICATES.VIEW'
WHERE r.code IN ('ADMIN', 'SUPERVISOR')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'CERTIFICATES.DOWNLOAD_ANY'
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
