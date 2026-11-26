-- Etapa 4: autorización granular para actividad, importación, histórico,
-- requisitos operativos y versiones de programación.
-- Se conservan los accesos de los roles existentes que tenían acceso autenticado
-- o restricciones heredadas por rol en estos endpoints.

-- Acciones específicas de Operaciones.
INSERT INTO permissions (module_id, code, name, description)
SELECT m.id, x.code, x.name, x.description
FROM permission_modules m
CROSS JOIN (VALUES
    ('OPERATIONS.ACTIVITY_VIEW', 'Ver actividad', 'Consultar la actividad de una operación'),
    ('OPERATIONS.IMPORT', 'Importar operaciones', 'Previsualizar, analizar y ejecutar importaciones'),
    ('OPERATIONS.IMPORT_HISTORY_VIEW', 'Ver historial de importaciones', 'Consultar historial y descargar archivos importados'),
    ('OPERATIONS.IMPORT_PROFILE_VIEW', 'Ver perfiles de importación', 'Consultar perfiles de importación'),
    ('OPERATIONS.IMPORT_PROFILE_MANAGE', 'Administrar perfiles de importación', 'Crear y eliminar perfiles de importación')
) AS x(code, name, description)
WHERE m.code = 'OPERATIONS'
ON CONFLICT (code) DO NOTHING;

-- Acciones específicas de programación.
INSERT INTO permissions (module_id, code, name, description)
SELECT m.id, x.code, x.name, x.description
FROM permission_modules m
CROSS JOIN (VALUES
    ('SCHEDULING.VERSION_VIEW', 'Ver versiones', 'Consultar versiones guardadas de programación'),
    ('SCHEDULING.VERSION_CREATE', 'Crear versiones', 'Guardar una nueva versión de programación')
) AS x(code, name, description)
WHERE m.code = 'SCHEDULING'
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (module_id, code, name, description)
SELECT m.id, 'HISTORY.RESTORE', 'Restaurar', 'Restaurar operaciones archivadas'
FROM permission_modules m
WHERE m.code = 'HISTORY'
ON CONFLICT (code) DO NOTHING;

-- Módulo independiente para el catálogo de requisitos.
INSERT INTO permission_modules (code, name, description, display_order)
VALUES ('REQUIREMENTS', 'Requisitos', 'Catálogo y asignación de requisitos operativos', 165)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (module_id, code, name, description)
SELECT m.id, x.code, x.name, x.description
FROM permission_modules m
CROSS JOIN (VALUES
    ('REQUIREMENTS.VIEW', 'Ver', 'Consultar el catálogo de requisitos'),
    ('REQUIREMENTS.CUSTOMER_ORDER_VIEW', 'Ver requisitos de O/S cliente', 'Consultar requisitos asociados a órdenes de servicio de clientes'),
    ('REQUIREMENTS.INTERNAL_ORDER_VIEW', 'Ver requisitos de O/S interna', 'Consultar requisitos asociados a órdenes de servicio internas'),
    ('REQUIREMENTS.OPERATION_VIEW', 'Ver requisitos de operación', 'Consultar requisitos asociados a operaciones'),
    ('REQUIREMENTS.CREATE', 'Crear', 'Crear requisitos del catálogo'),
    ('REQUIREMENTS.EDIT', 'Editar', 'Modificar requisitos del catálogo')
) AS x(code, name, description)
WHERE m.code = 'REQUIREMENTS'
ON CONFLICT (code) DO NOTHING;

-- Acceso histórico a la actividad y consulta de requisitos: todos los roles
-- existentes podían consultar estos endpoints autenticados.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'OPERATIONS.ACTIVITY_VIEW',
    'HISTORY.VIEW',
    'REQUIREMENTS.VIEW',
    'REQUIREMENTS.CUSTOMER_ORDER_VIEW',
    'REQUIREMENTS.INTERNAL_ORDER_VIEW',
    'REQUIREMENTS.OPERATION_VIEW',
    'SCHEDULING.VERSION_VIEW'
)
WHERE r.code IN ('ADMIN', 'SUPERVISOR', 'CHOFER', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- La importación estaba limitada a ADMIN, SUPERVISOR y OPERACIONES.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'OPERATIONS.IMPORT',
    'OPERATIONS.IMPORT_HISTORY_VIEW',
    'OPERATIONS.IMPORT_PROFILE_VIEW',
    'OPERATIONS.IMPORT_PROFILE_MANAGE',
    'SCHEDULING.VERSION_CREATE',
    'REQUIREMENTS.CREATE',
    'REQUIREMENTS.EDIT'
)
WHERE r.code IN ('ADMIN', 'SUPERVISOR', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- La consulta y modificación de requisitos vinculados a órdenes/operaciones
-- sigue el permiso del recurso padre; V43 ya conserva los accesos de esos roles.

-- Restaurar operaciones archivadas estaba reservado a ADMIN y SUPERVISOR.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'HISTORY.RESTORE'
WHERE r.code IN ('ADMIN', 'SUPERVISOR')
ON CONFLICT (role_id, permission_id) DO NOTHING;
