-- Etapa 4: autorización granular del módulo de capacitación.
-- Se separan cursos, preguntas, audiencias, materiales, progreso y evaluaciones
-- para conservar las diferencias de acceso que existían antes de la migración.

WITH definitions(code, name, description) AS (
    VALUES
    ('TRAINING.COURSES.VIEW', 'Consultar cursos', 'Consultar cursos y cursos asignados'),
    ('TRAINING.COURSES.CREATE', 'Crear cursos', 'Crear cursos'),
    ('TRAINING.COURSES.EDIT', 'Editar cursos', 'Modificar cursos'),
    ('TRAINING.COURSES.DELETE', 'Desactivar cursos', 'Desactivar cursos'),
    ('TRAINING.AUDIENCE.VIEW', 'Consultar audiencias', 'Consultar audiencias de cursos'),
    ('TRAINING.AUDIENCE.CREATE', 'Asignar audiencias', 'Asignar audiencias a cursos'),
    ('TRAINING.AUDIENCE.DELETE', 'Retirar audiencias', 'Retirar audiencias de cursos'),
    ('TRAINING.QUESTIONS.VIEW', 'Consultar preguntas', 'Consultar y descargar plantillas de preguntas'),
    ('TRAINING.QUESTIONS.CREATE', 'Crear preguntas', 'Crear preguntas'),
    ('TRAINING.QUESTIONS.EDIT', 'Editar preguntas', 'Editar o activar preguntas'),
    ('TRAINING.QUESTIONS.DELETE', 'Eliminar preguntas', 'Eliminar preguntas'),
    ('TRAINING.QUESTIONS.ASSIGN', 'Asignar preguntas', 'Asignar preguntas a cursos'),
    ('TRAINING.QUESTIONS.REMOVE', 'Retirar preguntas', 'Retirar preguntas de cursos'),
    ('TRAINING.QUESTIONS.IMPORT', 'Importar preguntas', 'Importar preguntas desde archivo'),
    ('TRAINING.MATERIALS.VIEW', 'Consultar materiales', 'Consultar materiales y contenido'),
    ('TRAINING.MATERIALS.CREATE', 'Crear materiales', 'Crear y cargar materiales'),
    ('TRAINING.MATERIALS.EDIT', 'Editar materiales', 'Modificar y activar materiales'),
    ('TRAINING.MATERIALS.DELETE', 'Eliminar materiales', 'Eliminar materiales'),
    ('TRAINING.PROGRESS.VIEW', 'Consultar progreso', 'Consultar el progreso propio de materiales'),
    ('TRAINING.PROGRESS.EDIT', 'Actualizar progreso', 'Actualizar el progreso propio de materiales'),
    ('TRAINING.EVALUATIONS.VIEW', 'Consultar evaluaciones', 'Consultar evaluaciones propias y pendientes'),
    ('TRAINING.EVALUATIONS.EDIT', 'Gestionar intento propio', 'Iniciar, responder, reanudar o cancelar evaluaciones propias'),
    ('TRAINING.EVALUATIONS.ADMIN', 'Administrar evaluaciones', 'Consultar intentos, rehabilitar y administrar evaluaciones')
)
INSERT INTO permissions (module_id, code, name, description)
SELECT pm.id, d.code, d.name, d.description
FROM permission_modules pm
CROSS JOIN definitions d
WHERE pm.code = 'TRAINING'
ON CONFLICT (code) DO NOTHING;

-- ADMIN recibe todos los permisos del módulo, incluidos los nuevos.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN' AND p.code LIKE 'TRAINING.%'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Supervisor conserva la administración de cursos/preguntas y la carga de materiales.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code IN (
    'TRAINING.COURSES.VIEW','TRAINING.COURSES.CREATE','TRAINING.COURSES.EDIT','TRAINING.COURSES.DELETE',
    'TRAINING.AUDIENCE.VIEW','TRAINING.AUDIENCE.CREATE','TRAINING.AUDIENCE.DELETE',
    'TRAINING.QUESTIONS.VIEW','TRAINING.QUESTIONS.CREATE','TRAINING.QUESTIONS.EDIT',
    'TRAINING.QUESTIONS.DELETE','TRAINING.QUESTIONS.ASSIGN','TRAINING.QUESTIONS.REMOVE','TRAINING.QUESTIONS.IMPORT',
    'TRAINING.MATERIALS.VIEW','TRAINING.MATERIALS.CREATE',
    'TRAINING.PROGRESS.VIEW','TRAINING.PROGRESS.EDIT',
    'TRAINING.EVALUATIONS.VIEW','TRAINING.EVALUATIONS.EDIT','TRAINING.EVALUATIONS.ADMIN'
)
WHERE r.code = 'SUPERVISOR'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Todos los roles existentes conservan la consulta y participación personal en capacitación.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.code IN (
    'TRAINING.COURSES.VIEW','TRAINING.AUDIENCE.VIEW','TRAINING.QUESTIONS.VIEW',
    'TRAINING.MATERIALS.VIEW','TRAINING.PROGRESS.VIEW','TRAINING.PROGRESS.EDIT',
    'TRAINING.EVALUATIONS.VIEW','TRAINING.EVALUATIONS.EDIT'
)
WHERE r.code IN ('ADMIN','SUPERVISOR','CHOFER','OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;
