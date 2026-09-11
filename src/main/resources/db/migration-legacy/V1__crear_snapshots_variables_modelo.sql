CREATE TABLE IF NOT EXISTS snapshots_variables_modelo (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    schema_version VARCHAR(80) NOT NULL,
    fecha_corte DATE NOT NULL,
    ventana_habitos_dias INTEGER NOT NULL,
    contenido_json TEXT NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_snapshots_variables_cliente_corte
    ON snapshots_variables_modelo (cliente_id, fecha_corte);
