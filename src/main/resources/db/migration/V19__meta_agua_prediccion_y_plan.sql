ALTER TABLE predicciones_modelo
    ADD COLUMN IF NOT EXISTS meta_agua_ml NUMERIC(19, 2);

ALTER TABLE planes_diarios
    ADD COLUMN IF NOT EXISTS agua_min_ml NUMERIC(19, 2),
    ADD COLUMN IF NOT EXISTS agua_max_ml NUMERIC(19, 2);

ALTER TABLE sesiones_conocimiento_ia
    ADD COLUMN IF NOT EXISTS meta_agua_ml NUMERIC(19, 2);
