-- Base de autorización granular. La aplicación seguirá usando users.role durante la migración.
CREATE TABLE permission_modules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(300),
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(300),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    protected_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES permission_modules(id),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(300),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (username, role_id)
);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_permissions_module_id ON permissions(module_id);

CREATE TABLE authorization_audit (
    id BIGSERIAL PRIMARY KEY,
    actor_username VARCHAR(50) REFERENCES users(username) ON DELETE SET NULL,
    action VARCHAR(40) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_authorization_audit_created_at ON authorization_audit(created_at DESC);
CREATE INDEX idx_authorization_audit_target ON authorization_audit(target_type, target_id);

-- Se registran los cuatro roles existentes sin retirar aún users.role.
INSERT INTO roles (code, name, description, active, system_role, protected_role) VALUES
('ADMIN', 'Administrador', 'Rol administrativo existente', TRUE, TRUE, TRUE),
('SUPERVISOR', 'Supervisor', 'Rol supervisor existente', TRUE, TRUE, FALSE),
('CHOFER', 'Chofer', 'Rol de conductor existente', TRUE, TRUE, FALSE),
('OPERACIONES', 'Operaciones', 'Rol de operaciones existente', TRUE, TRUE, FALSE);

-- Catálogo inicial de módulos; los permisos se amplían de forma controlada en las siguientes etapas.
INSERT INTO permission_modules (code, name, description, display_order) VALUES
('DASHBOARD', 'Dashboard', 'Panel principal', 10),
('CLIENTS', 'Clientes', 'Gestión de clientes y relaciones', 20),
('LOCATIONS', 'Ubicaciones', 'Gestión de ubicaciones', 30),
('OPERATIONS', 'Operaciones', 'Gestión de operaciones', 40),
('CUSTOMER_ORDERS', 'O/S cliente', 'Órdenes de servicio de clientes', 50),
('INTERNAL_ORDERS', 'O/S internas', 'Órdenes de servicio internas', 60),
('SCHEDULING', 'Planificador', 'Programación semanal', 70),
('FLEET', 'Flota', 'Vehículos y asignaciones', 80),
('DRIVERS', 'Conductores', 'Gestión de conductores', 90),
('WAREHOUSES', 'Almacenes', 'Gestión de almacenes', 100),
('TRAINING', 'Capacitaciones', 'Cursos, preguntas y evaluaciones', 110),
('AUDIT', 'Auditoría', 'Consulta de auditoría', 120),
('HISTORY', 'Histórico', 'Histórico de operaciones', 130),
('USERS', 'Usuarios', 'Administración de usuarios', 140),
('ROLES', 'Roles y permisos', 'Administración de roles y permisos', 150),
('CONFIGURATION', 'Configuración', 'Configuración general', 160);

-- Acciones comunes. Acciones específicas se incorporarán junto con cada módulo.
INSERT INTO permissions (module_id, code, name, description)
SELECT m.id, m.code || '.' || a.code, a.name, a.description
FROM permission_modules m
CROSS JOIN (VALUES
    ('VIEW', 'Ver', 'Consultar información del módulo'),
    ('CREATE', 'Crear', 'Crear registros'),
    ('EDIT', 'Editar', 'Modificar registros'),
    ('DELETE', 'Eliminar', 'Desactivar o eliminar según permita el módulo'),
    ('EXPORT', 'Exportar', 'Exportar información'),
    ('ADMIN', 'Administrar', 'Administrar configuración del módulo')
) AS a(code, name, description)
WHERE m.code <> 'DASHBOARD';

-- El administrador protegido recibe todos los permisos registrados y futuros.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN';

-- Migración inicial de asignaciones: conserva el rol actual de cada usuario.
INSERT INTO user_roles (username, role_id)
SELECT u.username, r.id
FROM users u
JOIN roles r ON r.code = u.role::text
ON CONFLICT (username, role_id) DO NOTHING;
