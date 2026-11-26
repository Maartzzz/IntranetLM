-- Amplía el catálogo central de estados existente; no crea un catálogo paralelo.
ALTER TABLE statuses
    ADD COLUMN IF NOT EXISTS is_initial BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_final BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS auto_transition BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS allows_scheduling BOOLEAN NOT NULL DEFAULT TRUE;

INSERT INTO statuses (process_type_id, name, description, color, active, is_initial, is_final, auto_transition, allows_scheduling)
SELECT p.id, v.name, v.description, v.color, TRUE, v.is_initial, v.is_final, v.auto_transition, v.allows_scheduling
FROM process_types p
CROSS JOIN (VALUES
 ('Pendiente de programación', 'La O/S interna fue creada y aún no tiene fecha asignada.', '#64748B', TRUE, FALSE, FALSE, TRUE),
 ('Programada', 'La O/S interna tiene una fecha de programación asignada.', '#2563EB', FALSE, FALSE, FALSE, TRUE),
 ('En ejecución', 'La ejecución logística de la O/S interna está en curso.', '#D97706', FALSE, FALSE, FALSE, TRUE),
 ('Completada', 'La ejecución de la O/S interna terminó.', '#16A34A', FALSE, TRUE, FALSE, FALSE),
 ('Vencida', 'La fecha programada pasó y la O/S interna continúa pendiente.', '#DC2626', FALSE, FALSE, TRUE, TRUE)
) AS v(name, description, color, is_initial, is_final, auto_transition, allows_scheduling)
WHERE p.code = 'OS_INTERNA'
ON CONFLICT (process_type_id, name) DO UPDATE SET
 description = EXCLUDED.description,
 color = EXCLUDED.color,
 active = TRUE,
 is_initial = EXCLUDED.is_initial,
 is_final = EXCLUDED.is_final,
 auto_transition = EXCLUDED.auto_transition,
 allows_scheduling = EXCLUDED.allows_scheduling;

UPDATE statuses s
SET is_final = TRUE, allows_scheduling = FALSE
FROM process_types p
WHERE s.process_type_id = p.id AND p.code = 'OS_INTERNA' AND lower(s.name) = 'cancelada';

-- Normaliza el estado inicial de órdenes existentes sin estado.
UPDATE internal_service_orders o
SET status_id = s.id
FROM statuses s
JOIN process_types p ON p.id = s.process_type_id
WHERE p.code = 'OS_INTERNA'
  AND lower(s.name) = CASE WHEN o.scheduled_date IS NULL THEN lower('Pendiente de programación') ELSE lower('Programada') END
  AND o.status_id IS NULL;
