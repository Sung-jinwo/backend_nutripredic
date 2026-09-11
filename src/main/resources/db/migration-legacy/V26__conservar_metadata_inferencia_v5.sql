ALTER TABLE predicciones_modelo
    ADD COLUMN inferred_at TIMESTAMPTZ;

ALTER TABLE predicciones_modelo
    ADD COLUMN inference_ms NUMERIC(14,6);
