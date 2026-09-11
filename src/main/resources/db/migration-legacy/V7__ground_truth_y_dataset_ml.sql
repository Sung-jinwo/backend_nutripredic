CREATE TABLE rubricas_perfil_habitos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(2000),
    estado VARCHAR(20) NOT NULL,
    vigente_desde TIMESTAMPTZ,
    validado_por VARCHAR(255),
    validado_en TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rubrica_perfil_codigo_version UNIQUE (codigo, version)
);

CREATE TABLE criterios_clasificacion_perfil (
    id BIGSERIAL PRIMARY KEY,
    rubrica_id BIGINT NOT NULL REFERENCES rubricas_perfil_habitos(id),
    clasificacion VARCHAR(20) NOT NULL,
    limite_inferior NUMERIC(6,2) NOT NULL,
    limite_superior NUMERIC(6,2) NOT NULL,
    incluye_inferior BOOLEAN NOT NULL,
    incluye_superior BOOLEAN NOT NULL,
    orden INTEGER NOT NULL,
    CONSTRAINT uq_criterio_perfil_rubrica_clasificacion UNIQUE (rubrica_id, clasificacion),
    CONSTRAINT uq_criterio_perfil_rubrica_orden UNIQUE (rubrica_id, orden)
);

CREATE TABLE evaluaciones_perfil_habitos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    rubrica_id BIGINT NOT NULL REFERENCES rubricas_perfil_habitos(id),
    evaluador_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_evaluacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_corte DATE NOT NULL,
    puntaje_total NUMERIC(6,2) NOT NULL,
    clasificacion_real VARCHAR(20) NOT NULL,
    estado_validez VARCHAR(30) NOT NULL,
    observacion VARCHAR(2000),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_evaluacion_perfil_puntaje CHECK (puntaje_total >= 0 AND puntaje_total <= 100)
);

CREATE INDEX ix_evaluacion_perfil_rubrica_validez
    ON evaluaciones_perfil_habitos (rubrica_id, estado_validez);
CREATE INDEX ix_evaluacion_perfil_cliente_corte
    ON evaluaciones_perfil_habitos (cliente_id, fecha_corte);
