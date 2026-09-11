ALTER TABLE sesiones_conocimiento_ia
    ADD COLUMN IF NOT EXISTS fecha_evaluacion DATE NULL,
    ADD COLUMN IF NOT EXISTS cliente_id BIGINT NULL REFERENCES clientes(id),
    ADD COLUMN IF NOT EXISTS objetivo_cliente VARCHAR(160) NULL,
    ADD COLUMN IF NOT EXISTS clasificacion_predictiva VARCHAR(40) NULL,
    ADD COLUMN IF NOT EXISTS puntaje_obtenido NUMERIC(5, 2) NULL,
    ADD COLUMN IF NOT EXISTS puntaje_maximo NUMERIC(5, 2) NULL,
    ADD COLUMN IF NOT EXISTS nivel_resultado VARCHAR(20) NULL;

UPDATE sesiones_conocimiento_ia s
SET fecha_evaluacion = p.fecha_corte,
    cliente_id = p.cliente_id,
    objetivo_cliente = CONCAT_WS(' | ', c.tipo_objetivo_fisico, c.objetivo_energetico, NULLIF(c.objetivo_fisico, '')),
    clasificacion_predictiva = p.clasificacion_predicha,
    puntaje_maximo = CASE WHEN s.estado = 'RESPONDIDA' THEN 10 ELSE s.puntaje_maximo END,
    puntaje_obtenido = CASE
        WHEN s.estado = 'RESPONDIDA' THEN (
            SELECT COUNT(*) * 2
            FROM respuestas_adaptativas_ia r
            WHERE r.sesion_id = s.id AND r.correcta = TRUE
        )
        ELSE s.puntaje_obtenido
    END
FROM predicciones_modelo p
JOIN clientes c ON c.id = p.cliente_id
WHERE p.id = s.prediccion_modelo_id
  AND s.fecha_evaluacion IS NULL;

UPDATE sesiones_conocimiento_ia
SET nivel_resultado = CASE
    WHEN puntaje_obtenido = 10 THEN 'ALTO'
    WHEN puntaje_obtenido >= 7 THEN 'INTERMEDIO'
    WHEN puntaje_obtenido IS NOT NULL THEN 'BAJO'
    ELSE NULL
END
WHERE nivel_resultado IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_sesion_conocimiento_cliente_fecha
    ON sesiones_conocimiento_ia (cliente_id, fecha_evaluacion)
    WHERE cliente_id IS NOT NULL AND fecha_evaluacion IS NOT NULL;
