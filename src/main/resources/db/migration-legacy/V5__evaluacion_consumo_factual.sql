CREATE TABLE IF NOT EXISTS ingredientes_catalogo (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
ALTER TABLE suplementos_ingredientes ADD COLUMN IF NOT EXISTS ingrediente_id BIGINT REFERENCES ingredientes_catalogo(id);
ALTER TABLE unidades_medida ADD COLUMN IF NOT EXISTS dimension VARCHAR(40);
ALTER TABLE unidades_medida ADD COLUMN IF NOT EXISTS factor_base NUMERIC(20,8);
ALTER TABLE suplementos_catalogo ADD COLUMN IF NOT EXISTS cantidad_por_porcion NUMERIC(12,4);
ALTER TABLE suplementos_catalogo ADD COLUMN IF NOT EXISTS unidad_porcion_id BIGINT REFERENCES unidades_medida(id);

CREATE TABLE IF NOT EXISTS criterios_consumo (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    estado VARCHAR(30) NOT NULL,
    ventana_dias INTEGER,
    fuente_referencia VARCHAR(1000),
    vigente_desde TIMESTAMPTZ,
    validado_por VARCHAR(255),
    validado_en TIMESTAMPTZ,
    version_evaluador VARCHAR(80),
    metadata_validacion VARCHAR(4000),
    regla_global VARCHAR(4000),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_criterio_consumo_version UNIQUE(codigo, version)
);
CREATE TABLE IF NOT EXISTS reglas_criterio_consumo (
    id BIGSERIAL PRIMARY KEY,
    criterio_id BIGINT NOT NULL REFERENCES criterios_consumo(id),
    alcance VARCHAR(30) NOT NULL,
    elemento_aplicable VARCHAR(255),
    metrica VARCHAR(100),
    unidad_referencia VARCHAR(30),
    tipo_evaluador VARCHAR(100),
    parametros VARCHAR(4000)
);
CREATE TABLE IF NOT EXISTS evaluaciones_consumo (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    participacion_estudio_id BIGINT REFERENCES participaciones_estudio(id),
    criterio_consumo_id BIGINT REFERENCES criterios_consumo(id),
    fecha_corte DATE NOT NULL,
    fecha_inicio DATE NOT NULL,
    ventana_dias INTEGER NOT NULL,
    momento VARCHAR(30) NOT NULL DEFAULT 'NO_DETERMINADO',
    estado_validez VARCHAR(30) NOT NULL DEFAULT 'NO_DETERMINADA',
    motivo_invalidez VARCHAR(1000),
    alto_consumo BOOLEAN,
    fecha_evaluacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    schema_version VARCHAR(80) NOT NULL,
    advertencias VARCHAR(4000)
);
CREATE TABLE IF NOT EXISTS snapshots_evaluacion_consumo (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_consumo_id BIGINT NOT NULL UNIQUE REFERENCES evaluaciones_consumo(id),
    schema_version VARCHAR(80) NOT NULL,
    contenido_json TEXT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS resultados_componentes_consumo (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_consumo_id BIGINT NOT NULL REFERENCES evaluaciones_consumo(id),
    alcance VARCHAR(30) NOT NULL,
    identificador VARCHAR(255),
    nombre VARCHAR(500) NOT NULL,
    cantidad_observada NUMERIC(18,6),
    unidad_original VARCHAR(30),
    cantidad_normalizada NUMERIC(18,6),
    unidad_normalizada VARCHAR(30),
    dias_con_registro INTEGER NOT NULL DEFAULT 0,
    cantidad_registros INTEGER NOT NULL DEFAULT 0,
    regla_criterio_id BIGINT REFERENCES reglas_criterio_consumo(id),
    evaluado BOOLEAN NOT NULL DEFAULT FALSE,
    resultado BOOLEAN,
    advertencia VARCHAR(1000)
);
CREATE INDEX IF NOT EXISTS ix_evaluacion_consumo_cliente_corte ON evaluaciones_consumo(cliente_id, fecha_corte);
