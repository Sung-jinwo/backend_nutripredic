ALTER TABLE sesiones_conocimiento_ia
    ADD COLUMN estado_validez VARCHAR(30) NOT NULL DEFAULT 'NO_DETERMINADA',
    ADD COLUMN motivo_invalidez VARCHAR(255) NULL;

ALTER TABLE sesiones_conocimiento_ia
    ADD CONSTRAINT chk_sesion_conocimiento_validez
    CHECK (estado_validez IN ('VALIDA', 'INVALIDA', 'NO_DETERMINADA'));

UPDATE sesiones_conocimiento_ia s
SET estado_validez = 'VALIDA',
    motivo_invalidez = NULL
FROM predicciones_modelo p
WHERE p.id = s.prediccion_modelo_id
  AND s.estado = 'RESPONDIDA'
  AND s.configuracion_version = 'pcc-ia-v1'
  AND p.estado = 'EXITOSA'
  AND p.momento_evaluacion = 'DIARIO'
  AND p.model_version = 'technical-v6-daily-002'
  AND s.puntaje_maximo = 10
  AND s.puntaje_obtenido IS NOT NULL
  AND s.nivel_resultado IN ('BAJO', 'INTERMEDIO', 'ALTO')
  AND (SELECT COUNT(*) FROM preguntas_generadas_ia q WHERE q.sesion_id = s.id) = 5
  AND (SELECT COUNT(*) FROM respuestas_adaptativas_ia r WHERE r.sesion_id = s.id) = 5;

UPDATE sesiones_conocimiento_ia
SET estado_validez = 'INVALIDA',
    motivo_invalidez = 'SESION_HISTORICA_NO_CUMPLE_CONTRATO_PCC_DIARIO'
WHERE estado = 'RESPONDIDA'
  AND estado_validez <> 'VALIDA';

CREATE INDEX idx_sesion_pcc_oficial
    ON sesiones_conocimiento_ia (cliente_id, fecha_evaluacion DESC, respondida_en DESC)
    WHERE estado = 'RESPONDIDA' AND estado_validez = 'VALIDA';

ALTER TABLE eventos_analisis
    ADD COLUMN estado_ciclo_diario VARCHAR(20) NULL,
    ADD COLUMN ciclo_completado_en TIMESTAMP WITH TIME ZONE NULL,
    ADD COLUMN procesamiento_ciclo_ms BIGINT NULL,
    ADD COLUMN modulo_fallo_ciclo VARCHAR(40) NULL,
    ADD COLUMN motivo_fallo_ciclo VARCHAR(1000) NULL;

ALTER TABLE eventos_analisis
    ADD CONSTRAINT chk_evento_estado_ciclo_diario
        CHECK (estado_ciclo_diario IN ('PENDIENTE', 'COMPLETADO', 'FALLIDO')),
    ADD CONSTRAINT chk_evento_procesamiento_ciclo
        CHECK (procesamiento_ciclo_ms IS NULL OR procesamiento_ciclo_ms >= 0);

CREATE INDEX idx_evento_tpp_ciclo_diario
    ON eventos_analisis (estado_ciclo_diario, momento, origen_resultado, fecha_corte);
