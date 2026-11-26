-- Separa el acceso al catálogo administrativo de cursos del acceso personal
-- a las capacitaciones asignadas al usuario.

INSERT INTO permissions (module_id, code, name, description)
SELECT pm.id,
       'TRAINING.MY_COURSES.VIEW',
       'Consultar mis capacitaciones',
       'Consultar los cursos asignados al usuario y su contenido de aprendizaje'
FROM permission_modules pm
WHERE pm.code = 'TRAINING'
ON CONFLICT (code) DO NOTHING;

-- Todos los roles operativos conservan el acceso a su espacio personal.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'TRAINING.MY_COURSES.VIEW'
WHERE r.code IN ('ADMIN', 'SUPERVISOR', 'CHOFER', 'OPERACIONES')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Los roles operativos no administradores dejan de tener acceso al catálogo.
-- ADMIN y SUPERVISOR conservan TRAINING.COURSES.VIEW según la matriz vigente.
DELETE FROM role_permissions rp
USING roles r, permissions p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.code IN ('CHOFER', 'OPERACIONES')
  AND p.code = 'TRAINING.COURSES.VIEW';
