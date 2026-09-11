CREATE TABLE estados_observacion_diaria (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    fecha DATE NOT NULL,
    dominio VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    confirmado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmado_por_id BIGINT REFERENCES usuarios(id),
    observacion VARCHAR(500),
    CONSTRAINT uq_estado_observacion_diaria UNIQUE (cliente_id, fecha, dominio)
);

CREATE INDEX ix_estado_observacion_diaria_intervalo
    ON estados_observacion_diaria(cliente_id, dominio, fecha);
