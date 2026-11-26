-- Etapa 4: autorización centralizada para configuración y catálogos.
-- Se preserva el acceso previo de usuarios autenticados y las capacidades de administración
-- que tenían los roles heredados ADMIN y SUPERVISOR.

INSERT INTO permission_modules (code, name, description, display_order)
VALUES ('CIC', 'CIC', 'Categorías y documentos del CIC', 170)
ON CONFLICT (code) DO NOTHING;

WITH definitions(module_code, code, name, description) AS (
    VALUES
    ('CONFIGURATION', 'CONFIGURATION.CARGO_TYPES.VIEW', 'Consultar tipos de carga', 'Consultar tipos de carga activos y su detalle'),
    ('CONFIGURATION', 'CONFIGURATION.CARGO_TYPES.CREATE', 'Crear tipos de carga', 'Registrar tipos de carga'),
    ('CONFIGURATION', 'CONFIGURATION.CARGO_TYPES.EDIT', 'Editar tipos de carga', 'Modificar tipos de carga'),
    ('CONFIGURATION', 'CONFIGURATION.CARGO_TYPES.DELETE', 'Cambiar estado de tipos de carga', 'Activar o desactivar tipos de carga'),
    ('CONFIGURATION', 'CONFIGURATION.PROVIDERS.VIEW', 'Consultar proveedores', 'Consultar proveedores activos y listado administrativo'),
    ('CONFIGURATION', 'CONFIGURATION.PROVIDERS.CREATE', 'Crear proveedores', 'Registrar proveedores'),
    ('CONFIGURATION', 'CONFIGURATION.PROVIDERS.EDIT', 'Editar proveedores', 'Modificar proveedores'),
    ('CONFIGURATION', 'CONFIGURATION.PROVIDERS.DELETE', 'Cambiar estado de proveedores', 'Activar o desactivar proveedores'),
    ('CONFIGURATION', 'CONFIGURATION.STATUSES.VIEW', 'Consultar estados', 'Consultar estados por proceso'),
    ('CONFIGURATION', 'CONFIGURATION.STATUSES.CREATE', 'Crear estados', 'Registrar estados de procesos'),
    ('CONFIGURATION', 'CONFIGURATION.STATUSES.EDIT', 'Editar estados', 'Modificar estados de procesos'),
    ('CIC', 'CIC.CATEGORIES.VIEW', 'Consultar categorías CIC', 'Consultar categorías activas y listado administrativo'),
    ('CIC', 'CIC.CATEGORIES.CREATE', 'Crear categorías CIC', 'Registrar categorías CIC'),
    ('CIC', 'CIC.CATEGORIES.EDIT', 'Editar categorías CIC', 'Modificar categorías CIC'),
    ('CIC', 'CIC.CATEGORIES.DELETE', 'Cambiar estado de categorías CIC', 'Activar o desactivar categorías CIC'),
    ('CIC', 'CIC.DOCUMENTS.VIEW', 'Consultar documentos CIC', 'Consultar documentos publicados y administración de documentos'),
    ('CIC', 'CIC.DOCUMENTS.CREATE', 'Crear documentos CIC', 'Cargar documentos CIC'),
    ('CIC', 'CIC.DOCUMENTS.EDIT', 'Editar documentos CIC', 'Cambiar el estado de documentos CIC')
)
INSERT INTO permissions (module_id, code, name, description)
SELECT pm.id, d.code, d.name, d.description
FROM definitions d
JOIN permission_modules pm ON pm.code = d.module_code
ON CONFLICT (code) DO NOTHING;

-- El rol protegido ADMIN recibe todos los permisos nuevos.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'CONFIGURATION.CARGO_TYPES.VIEW','CONFIGURATION.CARGO_TYPES.CREATE',
    'CONFIGURATION.CARGO_TYPES.EDIT','CONFIGURATION.CARGO_TYPES.DELETE',
    'CONFIGURATION.PROVIDERS.VIEW','CONFIGURATION.PROVIDERS.CREATE',
    'CONFIGURATION.PROVIDERS.EDIT','CONFIGURATION.PROVIDERS.DELETE',
    'CONFIGURATION.STATUSES.VIEW','CONFIGURATION.STATUSES.CREATE','CONFIGURATION.STATUSES.EDIT',
    'CIC.CATEGORIES.VIEW','CIC.CATEGORIES.CREATE','CIC.CATEGORIES.EDIT','CIC.CATEGORIES.DELETE',
    'CIC.DOCUMENTS.VIEW','CIC.DOCUMENTS.CREATE','CIC.DOCUMENTS.EDIT'
)
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- La consulta de catálogos sigue disponible para los cuatro roles preexistentes,
-- como ocurría cuando los endpoints solo exigían autenticación.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'CONFIGURATION.CARGO_TYPES.VIEW',
    'CONFIGURATION.PROVIDERS.VIEW',
    'CONFIGURATION.STATUSES.VIEW',
    'CIC.CATEGORIES.VIEW',
    'CIC.DOCUMENTS.VIEW'
)
WHERE r.code IN ('ADMIN','SUPERVISOR','CHOFER','OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- La administración de proveedores estaba disponible para ADMIN y SUPERVISOR.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'CONFIGURATION.PROVIDERS.CREATE',
    'CONFIGURATION.PROVIDERS.EDIT',
    'CONFIGURATION.PROVIDERS.DELETE'
)
WHERE r.code = 'SUPERVISOR'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Los endpoints de estados no restringían por rol; se conserva esa compatibilidad.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'CONFIGURATION.STATUSES.CREATE',
    'CONFIGURATION.STATUSES.EDIT'
)
WHERE r.code IN ('ADMIN','SUPERVISOR','CHOFER','OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;
