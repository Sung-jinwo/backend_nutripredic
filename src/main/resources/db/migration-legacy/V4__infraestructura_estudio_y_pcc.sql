CREATE TABLE IF NOT EXISTS estudios (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    fecha_inicio DATE,
    fecha_fin DATE,
    descripcion VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS participaciones_estudio (
    id BIGSERIAL PRIMARY KEY,
    estudio_id BIGINT NOT NULL REFERENCES estudios(id),
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    codigo_participante VARCHAR(80) NOT NULL,
    grupo VARCHAR(30) NOT NULL,
    fecha_asignacion DATE,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_participacion_estudio_cliente UNIQUE(estudio_id, cliente_id),
    CONSTRAINT uq_participacion_estudio_codigo UNIQUE(estudio_id, codigo_participante)
);

CREATE TABLE IF NOT EXISTS temas_conocimiento (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS instrumentos_conocimiento (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    vigente_desde TIMESTAMPTZ,
    fuente_referencia VARCHAR(1000),
    metadata_validacion VARCHAR(4000),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_instrumento_codigo_version UNIQUE(codigo, version)
);

CREATE TABLE IF NOT EXISTS instrumentos_preguntas (
    id BIGSERIAL PRIMARY KEY,
    instrumento_id BIGINT NOT NULL REFERENCES instrumentos_conocimiento(id),
    pregunta_id BIGINT NOT NULL REFERENCES preguntas_conocimiento(id),
    orden INTEGER NOT NULL,
    puntuacion NUMERIC(12,4) NOT NULL,
    CONSTRAINT uq_instrumento_pregunta UNIQUE(instrumento_id, pregunta_id),
    CONSTRAINT uq_instrumento_orden UNIQUE(instrumento_id, orden)
);

CREATE TABLE IF NOT EXISTS instrumentos_temas (
    id BIGSERIAL PRIMARY KEY,
    instrumento_id BIGINT NOT NULL REFERENCES instrumentos_conocimiento(id),
    tema_id BIGINT NOT NULL REFERENCES temas_conocimiento(id),
    criterio_refuerzo VARCHAR(2000),
    CONSTRAINT uq_instrumento_tema UNIQUE(instrumento_id, tema_id)
);

CREATE TABLE IF NOT EXISTS criterios_clasificacion_instrumento (
    id BIGSERIAL PRIMARY KEY,
    instrumento_id BIGINT NOT NULL REFERENCES instrumentos_conocimiento(id),
    nivel VARCHAR(30) NOT NULL,
    parametros VARCHAR(2000),
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_criterio_instrumento_nivel UNIQUE(instrumento_id, nivel)
);

CREATE TABLE IF NOT EXISTS contenidos_educativos (
    id BIGSERIAL PRIMARY KEY,
    grupo_version VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    tema_id BIGINT REFERENCES temas_conocimiento(id),
    subtema VARCHAR(255),
    titulo VARCHAR(500) NOT NULL,
    resumen_breve VARCHAR(2000) NOT NULL,
    contenido_ampliado TEXT,
    fuente_referencia VARCHAR(1000),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vigente_desde TIMESTAMPTZ,
    CONSTRAINT uq_contenido_grupo_version UNIQUE(grupo_version, version)
);

ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS tema_id BIGINT REFERENCES temas_conocimiento(id);
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS instrumento_id BIGINT REFERENCES instrumentos_conocimiento(id);
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS participacion_estudio_id BIGINT REFERENCES participaciones_estudio(id);
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS momento VARCHAR(30) DEFAULT 'NO_DETERMINADO';
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS estado_validez VARCHAR(30) DEFAULT 'NO_DETERMINADA';
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS motivo_invalidez VARCHAR(1000);
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS puntaje_obtenido NUMERIC(12,4);
ALTER TABLE resultados_tests ADD COLUMN IF NOT EXISTS puntaje_maximo NUMERIC(12,4);
ALTER TABLE resultados_tema_test ADD COLUMN IF NOT EXISTS tema_id BIGINT REFERENCES temas_conocimiento(id);
ALTER TABLE resultados_tema_test ADD COLUMN IF NOT EXISTS puntaje_obtenido NUMERIC(12,4);
ALTER TABLE resultados_tema_test ADD COLUMN IF NOT EXISTS puntaje_maximo NUMERIC(12,4);
ALTER TABLE resultados_tema_test ADD COLUMN IF NOT EXISTS requiere_refuerzo BOOLEAN;
ALTER TABLE resultados_tema_test ADD COLUMN IF NOT EXISTS criterio_refuerzo_aplicado VARCHAR(2000);
CREATE INDEX IF NOT EXISTS ix_resultado_test_participacion_momento ON resultados_tests(participacion_estudio_id, momento, estado_validez);
