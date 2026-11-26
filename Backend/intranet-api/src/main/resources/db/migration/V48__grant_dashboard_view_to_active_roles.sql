-- Etapa 5: garantizar el acceso inicial al dashboard para los roles activos.
-- La ruta raíz redirige al dashboard cuando un usuario no tiene acceso a otra pantalla.
-- Sin este permiso, el guard podía redirigir indefinidamente al mismo dashboard.

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'DASHBOARD.VIEW'
WHERE r.active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
