CREATE TABLE predicciones_modelo (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    participacion_estudio_id BIGINT REFERENCES participaciones_estudio(id),
    momento_evaluacion VARCHAR(30) NOT NULL,
    fecha_corte DATE NOT NULL,
    clasificacion_predicha VARCHAR(20),
    prob_adecuado NUMERIC(8,6),
    prob_mejorable NUMERIC(8,6),
    prob_critico NUMERIC(8,6),
    model_version VARCHAR(255),
    schema_version VARCHAR(80) NOT NULL,
    fecha_prediccion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tiempo_inferencia_ms BIGINT,
    estado VARCHAR(20) NOT NULL,
    mensaje_error VARCHAR(2000),
    observaciones_tecnicas VARCHAR(2000)
);

CREATE INDEX ix_predicciones_modelo_cliente_fecha_prediccion
    ON predicciones_modelo (cliente_id, fecha_prediccion DESC);
CREATE INDEX ix_predicciones_modelo_cliente_fecha_corte
    ON predicciones_modelo (cliente_id, fecha_corte);
CREATE INDEX ix_predicciones_modelo_idempotencia
    ON predicciones_modelo (cliente_id, fecha_corte, momento_evaluacion, model_version, estado);
