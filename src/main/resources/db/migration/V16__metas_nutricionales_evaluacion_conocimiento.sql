ALTER TABLE sesiones_conocimiento_ia
    ADD COLUMN IF NOT EXISTS plan_diario_id BIGINT NULL REFERENCES planes_diarios(id),
    ADD COLUMN IF NOT EXISTS meta_kcal NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS meta_proteina_g NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS meta_carbohidratos_g NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS meta_grasas_g NUMERIC(19, 2) NULL;

CREATE INDEX IF NOT EXISTS idx_sesion_conocimiento_plan_diario
    ON sesiones_conocimiento_ia (plan_diario_id);
