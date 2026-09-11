ALTER TABLE registros_alimentos
  ALTER COLUMN alimento_id DROP NOT NULL,
  ADD COLUMN IF NOT EXISTS nombre_registrado VARCHAR(200);

UPDATE registros_alimentos r
SET nombre_registrado = a.nombre
FROM alimentos_catalogo a
WHERE r.alimento_id = a.id
  AND r.nombre_registrado IS NULL;

ALTER TABLE registros_alimentos
  ALTER COLUMN nombre_registrado SET NOT NULL;
