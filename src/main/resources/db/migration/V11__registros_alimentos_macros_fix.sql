-- Respaldo idempotente de V8: si una BD existente marcó V8 como aplicada
-- sin crear las columnas (baseline manual, restore parcial, etc.),
-- esta migración garantiza que existan antes del validate de Hibernate.
-- No modifica V8 (regla: una migración ejecutada no se toca).
ALTER TABLE registros_alimentos
  ADD COLUMN IF NOT EXISTS kcal_registrada NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS proteina_g_registrada NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS carbohidratos_g_registrados NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS grasas_g_registradas NUMERIC(12,4);
