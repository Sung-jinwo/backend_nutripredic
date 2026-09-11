-- Fase 3.4: migración aditiva. No elimina columnas ni datos legacy.
CREATE TABLE IF NOT EXISTS unidades_medida (
    id BIGSERIAL PRIMARY KEY, codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL, activa BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO unidades_medida(codigo,nombre,activa) VALUES
('MG','Miligramo',TRUE),('G','Gramo',TRUE),('MCG','Microgramo',TRUE),('ML','Mililitro',TRUE),('L','Litro',TRUE),
('CAPSULA','Cápsula',TRUE),('TABLETA','Tableta',TRUE),('SCOOP','Scoop',TRUE),('PORCION','Porción',TRUE),('UNIDAD','Unidad',TRUE)
ON CONFLICT (codigo) DO NOTHING;

ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS grupo_version VARCHAR(36);
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS tema VARCHAR(255);
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS subtema VARCHAR(255);
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS explicacion VARCHAR(2000);
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS fuente_referencia VARCHAR(1000);
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS estado_pregunta VARCHAR(20) DEFAULT 'ACTIVA';
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS creado_en TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE preguntas_conocimiento ADD COLUMN IF NOT EXISTS vigente_desde TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
UPDATE preguntas_conocimiento SET grupo_version='LEGACY-' || id WHERE grupo_version IS NULL;
UPDATE preguntas_conocimiento SET version=1 WHERE version IS NULL;
UPDATE preguntas_conocimiento SET tema=categoria WHERE tema IS NULL;
UPDATE preguntas_conocimiento SET estado_pregunta='ACTIVA' WHERE estado_pregunta IS NULL;
UPDATE preguntas_conocimiento SET creado_en=CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE preguntas_conocimiento SET vigente_desde=creado_en WHERE vigente_desde IS NULL;
ALTER TABLE preguntas_conocimiento ALTER COLUMN grupo_version SET NOT NULL;
ALTER TABLE preguntas_conocimiento ALTER COLUMN version SET NOT NULL;
ALTER TABLE preguntas_conocimiento ALTER COLUMN estado_pregunta SET NOT NULL;
ALTER TABLE preguntas_conocimiento ALTER COLUMN creado_en SET NOT NULL;
ALTER TABLE preguntas_conocimiento ALTER COLUMN vigente_desde SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_pregunta_grupo_version ON preguntas_conocimiento(grupo_version,version);

CREATE TABLE IF NOT EXISTS resultados_tema_test (
    id BIGSERIAL PRIMARY KEY, resultado_id BIGINT NOT NULL REFERENCES resultados_tests(id),
    tema VARCHAR(255) NOT NULL, correctas INTEGER NOT NULL, total INTEGER NOT NULL,
    porcentaje DOUBLE PRECISION NOT NULL, CONSTRAINT uq_resultado_tema UNIQUE(resultado_id,tema)
);

ALTER TABLE suplementos_catalogo ADD COLUMN IF NOT EXISTS marca VARCHAR(150);
ALTER TABLE suplementos_catalogo ADD COLUMN IF NOT EXISTS presentacion VARCHAR(150);
ALTER TABLE suplementos_catalogo ADD COLUMN IF NOT EXISTS unidad_presentacion_id BIGINT REFERENCES unidades_medida(id);
ALTER TABLE suplementos_catalogo ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE suplementos_cliente ADD COLUMN IF NOT EXISTS cantidad_por_toma NUMERIC(12,4);
ALTER TABLE suplementos_cliente ADD COLUMN IF NOT EXISTS unidad_medida_id BIGINT REFERENCES unidades_medida(id);
ALTER TABLE suplementos_cliente ADD COLUMN IF NOT EXISTS tomas_por_periodo INTEGER;
ALTER TABLE suplementos_cliente ADD COLUMN IF NOT EXISTS periodo_frecuencia VARCHAR(20);
CREATE TABLE IF NOT EXISTS suplementos_ingredientes (
    id BIGSERIAL PRIMARY KEY, suplemento_catalogo_id BIGINT NOT NULL REFERENCES suplementos_catalogo(id),
    ingrediente VARCHAR(255) NOT NULL, cantidad_por_porcion NUMERIC(12,4) NOT NULL,
    unidad_medida_id BIGINT NOT NULL REFERENCES unidades_medida(id)
);
CREATE TABLE IF NOT EXISTS historial_suplementos_cliente (
    id BIGSERIAL PRIMARY KEY, asignacion_id BIGINT NOT NULL REFERENCES suplementos_cliente(id),
    cantidad DOUBLE PRECISION, unidad VARCHAR(255), frecuencia VARCHAR(255), tiempo_uso VARCHAR(255),
    cantidad_por_toma NUMERIC(12,4), unidad_medida_id BIGINT REFERENCES unidades_medida(id),
    tomas_por_periodo INTEGER, periodo_frecuencia VARCHAR(20), activo BOOLEAN,
    fecha_inicio DATE, fecha_fin DATE, registrado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO historial_suplementos_cliente(asignacion_id,cantidad,unidad,frecuencia,tiempo_uso,cantidad_por_toma,unidad_medida_id,tomas_por_periodo,periodo_frecuencia,activo,fecha_inicio,fecha_fin)
SELECT s.id,s.cantidad,s.unidad,s.frecuencia,s.tiempo_uso,s.cantidad_por_toma,s.unidad_medida_id,s.tomas_por_periodo,s.periodo_frecuencia,s.activo,s.fecha_inicio,s.fecha_fin FROM suplementos_cliente s
WHERE NOT EXISTS(SELECT 1 FROM historial_suplementos_cliente h WHERE h.asignacion_id=s.id);

CREATE TABLE IF NOT EXISTS alimentos_catalogo (
    id BIGSERIAL PRIMARY KEY, nombre VARCHAR(150) NOT NULL UNIQUE, categoria VARCHAR(100) NOT NULL,
    unidad_base_id BIGINT REFERENCES unidades_medida(id), activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS registros_alimentos (
    id BIGSERIAL PRIMARY KEY, registro_habito_id BIGINT NOT NULL REFERENCES registros_habitos(id),
    alimento_id BIGINT NOT NULL REFERENCES alimentos_catalogo(id), cantidad NUMERIC(12,4) NOT NULL,
    unidad_medida_id BIGINT NOT NULL REFERENCES unidades_medida(id), momento_comida VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_registro_alimento_habito ON registros_alimentos(registro_habito_id);
CREATE TABLE IF NOT EXISTS plantillas_comida (
    id BIGSERIAL PRIMARY KEY, cliente_id BIGINT NOT NULL REFERENCES clientes(id), nombre VARCHAR(150) NOT NULL,
    momento_comida VARCHAR(30) NOT NULL, activa BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS plantillas_comida_detalles (
    id BIGSERIAL PRIMARY KEY, plantilla_id BIGINT NOT NULL REFERENCES plantillas_comida(id),
    alimento_id BIGINT NOT NULL REFERENCES alimentos_catalogo(id), cantidad NUMERIC(12,4) NOT NULL,
    unidad_id BIGINT NOT NULL REFERENCES unidades_medida(id)
);

CREATE TABLE IF NOT EXISTS historial_perfiles_cliente (
    id BIGSERIAL PRIMARY KEY, cliente_id BIGINT NOT NULL REFERENCES clientes(id), edad INTEGER,
    peso_kg NUMERIC(5,2), altura_cm NUMERIC(5,2), objetivo_fisico VARCHAR(120),
    fecha_desde DATE NOT NULL, creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO historial_perfiles_cliente(cliente_id,edad,peso_kg,altura_cm,objetivo_fisico,fecha_desde)
SELECT c.id,c.edad,c.peso_kg,c.altura_cm,c.objetivo_fisico,CURRENT_DATE FROM clientes c
WHERE NOT EXISTS(SELECT 1 FROM historial_perfiles_cliente h WHERE h.cliente_id=c.id);
CREATE INDEX IF NOT EXISTS ix_historial_perfil_corte ON historial_perfiles_cliente(cliente_id,fecha_desde,creado_en);

CREATE TABLE IF NOT EXISTS eventos_analisis (
    id BIGSERIAL PRIMARY KEY, cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    datos_listos_en TIMESTAMPTZ, analisis_iniciado_en TIMESTAMPTZ,
    modelo_respondio_en TIMESTAMPTZ, resultado_disponible_en TIMESTAMPTZ
);
