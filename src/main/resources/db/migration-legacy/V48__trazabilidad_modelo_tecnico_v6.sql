ALTER TABLE predicciones_modelo
    ADD COLUMN IF NOT EXISTS model_type VARCHAR(80),
    ADD COLUMN IF NOT EXISTS training_data_type VARCHAR(80),
    ADD COLUMN IF NOT EXISTS is_thesis_final_model BOOLEAN,
    ADD COLUMN IF NOT EXISTS feature_count INTEGER;

ALTER TABLE predicciones_modelo
    ADD CONSTRAINT ck_prediccion_modelo_feature_count
        CHECK (feature_count IS NULL OR feature_count > 0);
