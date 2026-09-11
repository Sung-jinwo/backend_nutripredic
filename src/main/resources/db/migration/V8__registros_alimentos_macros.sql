ALTER TABLE registros_alimentos
  ADD COLUMN IF NOT EXISTS kcal_registrada NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS proteina_g_registrada NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS carbohidratos_g_registrados NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS grasas_g_registradas NUMERIC(12,4);
