CREATE TABLE IF NOT EXISTS registros_consumo_suplemento (
    id BIGSERIAL PRIMARY KEY,
    registro_habito_id BIGINT NOT NULL REFERENCES registros_habitos(id),
    suplemento_cliente_id BIGINT NOT NULL REFERENCES suplementos_cliente(id),
    cantidad_consumida NUMERIC(12,4) NOT NULL,
    unidad_medida_id BIGINT NOT NULL REFERENCES unidades_medida(id),
    numero_tomas INTEGER,
    observacion VARCHAR(500),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_consumo_habito_suplemento UNIQUE(registro_habito_id,suplemento_cliente_id),
    CONSTRAINT ck_consumo_cantidad_positiva CHECK(cantidad_consumida > 0),
    CONSTRAINT ck_consumo_tomas_positivas CHECK(numero_tomas IS NULL OR numero_tomas > 0)
);
CREATE INDEX IF NOT EXISTS ix_consumo_suplemento_habito ON registros_consumo_suplemento(registro_habito_id);
