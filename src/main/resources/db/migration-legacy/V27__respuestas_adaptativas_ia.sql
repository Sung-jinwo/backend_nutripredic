ALTER TABLE sesiones_conocimiento_ia
    ADD COLUMN respondida_en TIMESTAMPTZ;

CREATE TABLE respuestas_adaptativas_ia (
    id BIGSERIAL PRIMARY KEY,
    sesion_id BIGINT NOT NULL REFERENCES sesiones_conocimiento_ia(id),
    pregunta_generada_ia_id BIGINT NOT NULL REFERENCES preguntas_generadas_ia(id),
    opcion_seleccionada VARCHAR(1) NOT NULL,
    correcta BOOLEAN NOT NULL,
    respondida_en TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_respuesta_adaptativa_sesion_pregunta
        UNIQUE (sesion_id, pregunta_generada_ia_id)
);

CREATE INDEX idx_respuesta_adaptativa_sesion
    ON respuestas_adaptativas_ia (sesion_id);
