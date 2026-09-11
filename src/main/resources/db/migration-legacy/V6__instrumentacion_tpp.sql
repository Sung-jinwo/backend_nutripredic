CREATE TABLE IF NOT EXISTS procedimientos_analisis (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion_operativa VARCHAR(2000),
    fuente_referencia VARCHAR(1000),
    vigente_desde TIMESTAMPTZ,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    validado_por VARCHAR(255),
    validado_en TIMESTAMPTZ,
    CONSTRAINT uq_procedimiento_codigo_version UNIQUE(codigo, version)
);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS participacion_estudio_id BIGINT REFERENCES participaciones_estudio(id);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS procedimiento_analisis_id BIGINT REFERENCES procedimientos_analisis(id);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS momento VARCHAR(30) DEFAULT 'NO_DETERMINADO';
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS evaluador_usuario_id BIGINT REFERENCES usuarios(id);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS variables_preparadas_en TIMESTAMPTZ;
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS modelo_solicitado_en TIMESTAMPTZ;
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS estado_validez VARCHAR(30) DEFAULT 'NO_DETERMINADA';
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS motivo_invalidez VARCHAR(1000);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS prediccion_id BIGINT REFERENCES predicciones(id);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS snapshot_variables_modelo_id BIGINT REFERENCES snapshots_variables_modelo(id);
ALTER TABLE eventos_analisis ADD COLUMN IF NOT EXISTS observaciones_tecnicas VARCHAR(2000);
CREATE INDEX IF NOT EXISTS ix_evento_analisis_participacion_momento ON eventos_analisis(participacion_estudio_id, momento, estado_validez);
