-- Completa el catálogo de Dashboard (el resto de módulos recibió acciones comunes en V42).
INSERT INTO permissions (module_id, code, name, description)
SELECT id, 'DASHBOARD.VIEW', 'Ver', 'Consultar el panel principal'
FROM permission_modules WHERE code = 'DASHBOARD'
ON CONFLICT (code) DO NOTHING;

-- Compatibilidad: conserva los accesos de los roles heredados al migrar los
-- controladores principales a autorización por permisos.
-- Supervisor: clientes, ubicaciones, almacenes, operaciones, órdenes y flota.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON
    (p.code LIKE 'CLIENTS.%' OR p.code LIKE 'LOCATIONS.%' OR p.code LIKE 'WAREHOUSES.%'
     OR p.code LIKE 'OPERATIONS.%' OR p.code LIKE 'CUSTOMER_ORDERS.%'
     OR p.code LIKE 'INTERNAL_ORDERS.%' OR p.code LIKE 'FLEET.%' OR p.code LIKE 'DRIVERS.%')
WHERE r.code = 'SUPERVISOR'
  AND p.code NOT LIKE '%.ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Operaciones: operaciones, órdenes de cliente/internas y flota.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON
    (p.code LIKE 'OPERATIONS.%' OR p.code LIKE 'CUSTOMER_ORDERS.%'
     OR p.code LIKE 'INTERNAL_ORDERS.%' OR p.code LIKE 'FLEET.%')
WHERE r.code = 'OPERACIONES'
  AND p.code NOT LIKE '%.ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- El tablero semanal y la consulta de auditoría eran accesibles a usuarios
-- autenticados; se conserva esa disponibilidad para los roles preexistentes.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('SCHEDULING.VIEW', 'AUDIT.VIEW')
WHERE r.code IN ('ADMIN', 'SUPERVISOR', 'CHOFER', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Los controladores de operaciones y órdenes usan también SCHEDULING.VIEW
-- para los endpoints de consulta del planificador.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'SCHEDULING.VIEW'
WHERE r.code IN ('SUPERVISOR', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'SCHEDULING.EDIT'
WHERE r.code IN ('SUPERVISOR', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;
