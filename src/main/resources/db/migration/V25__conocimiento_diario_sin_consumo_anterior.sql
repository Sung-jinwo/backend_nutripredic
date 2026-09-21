-- Educational sessions never become official PCC by relabeling their source.
ALTER TABLE sesiones_conocimiento_ia DROP CONSTRAINT chk_sesion_inicial_sin_prediccion;
ALTER TABLE sesiones_conocimiento_ia ADD CONSTRAINT chk_sesion_inicial_sin_prediccion
    CHECK (prediccion_modelo_id IS NOT NULL OR
        (configuracion_version IN ('pcc-inicial-v1', 'pcc-perfil-diario-v1')
         AND cliente_id IS NOT NULL AND plan_diario_id IS NOT NULL));
-- Preserve one session per date/source. A later real prediction may coexist with
-- the educational session; its answers and source remain immutable history.
DROP INDEX uq_sesion_conocimiento_cliente_fecha;
CREATE UNIQUE INDEX uq_sesion_conocimiento_cliente_fecha
    ON sesiones_conocimiento_ia (cliente_id, fecha_evaluacion)
    WHERE cliente_id IS NOT NULL AND fecha_evaluacion IS NOT NULL AND prediccion_modelo_id IS NOT NULL;
CREATE UNIQUE INDEX uq_sesion_conocimiento_perfil_cliente_fecha
    ON sesiones_conocimiento_ia (cliente_id, fecha_evaluacion)
    WHERE cliente_id IS NOT NULL AND fecha_evaluacion IS NOT NULL AND prediccion_modelo_id IS NULL;
