ALTER TABLE predicciones_modelo
    ADD COLUMN IF NOT EXISTS meta_kcal NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS meta_proteina_g NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS meta_carbohidratos_g NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS meta_grasas_g NUMERIC(19, 2) NULL,
    ADD COLUMN IF NOT EXISTS formula_nutricional_version VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS fuente_formula_nutricional VARCHAR(500) NULL;
