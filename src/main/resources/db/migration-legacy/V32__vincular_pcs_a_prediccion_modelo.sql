ALTER TABLE evaluaciones_consumo
    ADD COLUMN IF NOT EXISTS prediccion_modelo_id BIGINT REFERENCES predicciones_modelo(id);
CREATE INDEX IF NOT EXISTS ix_evaluaciones_consumo_prediccion_modelo
    ON evaluaciones_consumo(prediccion_modelo_id);
