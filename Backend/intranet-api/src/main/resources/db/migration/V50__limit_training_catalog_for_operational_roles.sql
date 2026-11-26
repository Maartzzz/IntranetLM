-- Etapa 6: los roles operativos participan en sus capacitaciones asignadas,
-- pero no consultan el banco de preguntas ni la configuración de audiencias.
-- El catálogo de cursos ya fue separado en V49.

DELETE FROM role_permissions rp
USING roles r, permissions p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.code IN ('CHOFER', 'OPERACIONES')
  AND p.code IN (
      'TRAINING.QUESTIONS.VIEW',
      'TRAINING.AUDIENCE.VIEW'
  );
