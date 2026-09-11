CREATE TABLE sesiones_conocimiento_ia (
    id BIGSERIAL PRIMARY KEY,
    prediccion_modelo_id BIGINT NOT NULL REFERENCES predicciones_modelo(id),
    instrumento_id BIGINT REFERENCES instrumentos_conocimiento(id),
    configuracion_version VARCHAR(80) NOT NULL,
    model_version_predictivo VARCHAR(255) NOT NULL,
    schema_version VARCHAR(80) NOT NULL,
    proveedor_ia VARCHAR(50) NOT NULL,
    modelo_generativo VARCHAR(120) NOT NULL,
    estado VARCHAR(40) NOT NULL,
    generado_en TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_sesion_conocimiento_ia_prediccion_config UNIQUE(prediccion_modelo_id, configuracion_version)
);
CREATE TABLE preguntas_generadas_ia (
    id BIGSERIAL PRIMARY KEY,
    sesion_id BIGINT NOT NULL REFERENCES sesiones_conocimiento_ia(id),
    tema VARCHAR(255) NOT NULL,
    subtema VARCHAR(255),
    dificultad VARCHAR(50) NOT NULL,
    enunciado VARCHAR(2000) NOT NULL,
    opcion_a VARCHAR(1000) NOT NULL,
    opcion_b VARCHAR(1000) NOT NULL,
    opcion_c VARCHAR(1000) NOT NULL,
    opcion_d VARCHAR(1000) NOT NULL,
    respuesta_correcta VARCHAR(1) NOT NULL,
    explicacion VARCHAR(2000) NOT NULL,
    orden INTEGER NOT NULL,
    creada_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pregunta_generada_ia_sesion_orden UNIQUE(sesion_id, orden)
);
CREATE TABLE respuestas_preguntas_generadas_ia (
    id BIGSERIAL PRIMARY KEY,
    resultado_test_id BIGINT NOT NULL REFERENCES resultados_tests(id),
    pregunta_generada_ia_id BIGINT NOT NULL REFERENCES preguntas_generadas_ia(id),
    respuesta VARCHAR(1) NOT NULL,
    correcta BOOLEAN NOT NULL,
    CONSTRAINT uq_respuesta_generada_ia_resultado_pregunta UNIQUE(resultado_test_id, pregunta_generada_ia_id)
);
CREATE TABLE trazas_llamadas_gemini (
    id BIGSERIAL PRIMARY KEY,
    prediccion_modelo_id BIGINT NOT NULL REFERENCES predicciones_modelo(id),
    fecha TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exito BOOLEAN NOT NULL,
    cantidad_preguntas INTEGER NOT NULL,
    reutilizada BOOLEAN NOT NULL
);
