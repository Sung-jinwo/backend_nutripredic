ALTER TABLE sesiones_conocimiento_ia ALTER COLUMN prediccion_modelo_id DROP NOT NULL;
CREATE UNIQUE INDEX uq_conocimiento_inicial_cliente ON sesiones_conocimiento_ia (cliente_id)
    WHERE configuracion_version = 'pcc-inicial-v1';
ALTER TABLE sesiones_conocimiento_ia ADD CONSTRAINT chk_sesion_inicial_sin_prediccion
    CHECK (prediccion_modelo_id IS NOT NULL OR
        (configuracion_version = 'pcc-inicial-v1' AND cliente_id IS NOT NULL AND plan_diario_id IS NOT NULL));
