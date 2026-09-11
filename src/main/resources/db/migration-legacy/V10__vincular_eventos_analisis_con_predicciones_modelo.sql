ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS fecha_corte DATE;
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS prediccion_modelo_id BIGINT REFERENCES predicciones_modelo(id);
CREATE INDEX IF NOT EXISTS ix_evento_analisis_prediccion_modelo ON eventos_analisis(prediccion_modelo_id);
