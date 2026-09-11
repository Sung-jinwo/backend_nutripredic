ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS ambito_aporte VARCHAR(30);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS fuente_referencia_criterio VARCHAR(1000);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS version_referencia VARCHAR(100);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS observacion_metodologica VARCHAR(2000);
