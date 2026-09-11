-- V18: red de seguridad idempotente para ddl-auto=validate.
-- Si una BD marcó V13/V15/V16/V17 como aplicadas sin crear las columnas
-- (baseline manual, restore parcial), esto las garantiza.
-- En BDs al día es un no-op. No modifica migraciones ya ejecutadas.
ALTER TABLE sesiones_conocimiento_ia
  ADD COLUMN IF NOT EXISTS fecha_evaluacion DATE NULL,
  ADD COLUMN IF NOT EXISTS cliente_id BIGINT NULL REFERENCES clientes(id),
  ADD COLUMN IF NOT EXISTS objetivo_cliente VARCHAR(160) NULL,
  ADD COLUMN IF NOT EXISTS clasificacion_predictiva VARCHAR(40) NULL,
  ADD COLUMN IF NOT EXISTS puntaje_obtenido NUMERIC(5, 2) NULL,
  ADD COLUMN IF NOT EXISTS puntaje_maximo NUMERIC(5, 2) NULL,
  ADD COLUMN IF NOT EXISTS nivel_resultado VARCHAR(20) NULL,
  ADD COLUMN IF NOT EXISTS plan_diario_id BIGINT NULL REFERENCES planes_diarios(id),
  ADD COLUMN IF NOT EXISTS meta_kcal NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS meta_proteina_g NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS meta_carbohidratos_g NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS meta_grasas_g NUMERIC(19, 2) NULL;

ALTER TABLE predicciones_modelo
  ADD COLUMN IF NOT EXISTS meta_kcal NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS meta_proteina_g NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS meta_carbohidratos_g NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS meta_grasas_g NUMERIC(19, 2) NULL,
  ADD COLUMN IF NOT EXISTS formula_nutricional_version VARCHAR(100) NULL,
  ADD COLUMN IF NOT EXISTS fuente_formula_nutricional VARCHAR(500) NULL;

ALTER TABLE suplementos_cliente
  ADD COLUMN IF NOT EXISTS creatina_g_por_toma NUMERIC(12,4) NULL,
  ADD COLUMN IF NOT EXISTS cafeina_mg_por_toma NUMERIC(12,4) NULL,
  ADD COLUMN IF NOT EXISTS sodio_mg_por_toma NUMERIC(12,4) NULL;
