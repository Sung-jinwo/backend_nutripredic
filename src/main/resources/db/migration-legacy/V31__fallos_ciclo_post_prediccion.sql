CREATE TABLE fallos_ciclo_post_prediccion (
    id BIGSERIAL PRIMARY KEY,
    prediccion_modelo_id BIGINT NOT NULL REFERENCES predicciones_modelo(id),
    modulo VARCHAR(30) NOT NULL,
    tipo_error VARCHAR(160) NOT NULL,
    ocurrido_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detalle VARCHAR(1000)
);
CREATE INDEX ix_fallos_ciclo_post_prediccion_prediccion ON fallos_ciclo_post_prediccion(prediccion_modelo_id, ocurrido_en);
