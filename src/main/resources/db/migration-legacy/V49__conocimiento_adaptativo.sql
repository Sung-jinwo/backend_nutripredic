-- Conocimiento adaptativo: refuerzo educativo, no modifica PCC oficial
CREATE TABLE preguntas_conocimiento_ia (
    id BIGSERIAL PRIMARY KEY,
    tema VARCHAR(40) NOT NULL CHECK (tema IN ('PROTEINAS','CREATINA','CAFEINA','HIDRATACION','SUPLEMENTACION','ALIMENTACION_SALUDABLE')),
    subtema VARCHAR(100),
    dificultad VARCHAR(20) NOT NULL CHECK (dificultad IN ('BASICA','INTERMEDIA','AVANZADA')),
    enunciado VARCHAR(2000) NOT NULL,
    opcion_a VARCHAR(1000) NOT NULL,
    opcion_b VARCHAR(1000) NOT NULL,
    opcion_c VARCHAR(1000) NOT NULL,
    opcion_d VARCHAR(1000) NOT NULL,
    respuesta_correcta INT NOT NULL CHECK (respuesta_correcta BETWEEN 0 AND 3),
    explicacion VARCHAR(2000) NOT NULL,
    fuente VARCHAR(500) NOT NULL,
    modelo_ia VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA','INACTIVA','DESCARTADA')),
    creada_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pregunta_tema_dificultad_estado ON preguntas_conocimiento_ia(tema, dificultad, estado);

CREATE TABLE respuestas_conocimiento_ia (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    pregunta_id BIGINT NOT NULL REFERENCES preguntas_conocimiento_ia(id),
    respuesta_seleccionada INT NOT NULL CHECK (respuesta_seleccionada BETWEEN 0 AND 3),
    correcta BOOLEAN NOT NULL,
    respondida_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    intento INT NOT NULL
);
CREATE INDEX idx_respuesta_cliente_pregunta ON respuestas_conocimiento_ia(cliente_id, pregunta_id);
CREATE INDEX idx_respuesta_cliente_fecha ON respuestas_conocimiento_ia(cliente_id, respondida_en);

CREATE TABLE progreso_conocimiento_cliente (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    tema VARCHAR(40) NOT NULL CHECK (tema IN ('PROTEINAS','CREATINA','CAFEINA','HIDRATACION','SUPLEMENTACION','ALIMENTACION_SALUDABLE')),
    preguntas_respondidas INT NOT NULL DEFAULT 0,
    aciertos INT NOT NULL DEFAULT 0,
    errores INT NOT NULL DEFAULT 0,
    porcentaje_dominio DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    nivel_dominio VARCHAR(20) NOT NULL CHECK (nivel_dominio IN ('BAJO','MEDIO','ALTO')),
    ultima_revision TIMESTAMPTZ,
    proxima_revision DATE,
    racha_aciertos INT NOT NULL DEFAULT 0,
    racha_errores INT NOT NULL DEFAULT 0,
    UNIQUE(cliente_id, tema)
);
CREATE INDEX idx_progreso_cliente ON progreso_conocimiento_cliente(cliente_id);
