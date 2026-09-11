ALTER TABLE criterios_consumo
    ADD COLUMN IF NOT EXISTS vigente_hasta TIMESTAMPTZ;

ALTER TABLE criterios_consumo
    ADD COLUMN IF NOT EXISTS validada BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE criterios_consumo
    ADD COLUMN IF NOT EXISTS observacion VARCHAR(2000);

UPDATE criterios_consumo
SET validada = TRUE
WHERE validado_por IS NOT NULL
  AND validado_en IS NOT NULL;

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS suplemento_catalogo_id BIGINT
        REFERENCES suplementos_catalogo(id);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS componente_tipo VARCHAR(50);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS cantidad_referencia NUMERIC(18,6);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS unidad_normalizada VARCHAR(30);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS frecuencia VARCHAR(80);

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS numero_tomas INTEGER;

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS ventana_dias INTEGER;

ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS requiere_composicion BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE evaluaciones_consumo
    ADD COLUMN IF NOT EXISTS estado_clasificacion VARCHAR(30)
        NOT NULL DEFAULT 'NO_DETERMINADA';

ALTER TABLE evaluaciones_consumo
    ADD COLUMN IF NOT EXISTS motivo_clasificacion VARCHAR(100);

UPDATE evaluaciones_consumo
SET estado_clasificacion = 'NO_DETERMINADA',
    motivo_clasificacion = 'CRITERIO_NO_CONFIGURADO'
WHERE alto_consumo IS NULL
  AND motivo_clasificacion IS NULL;

CREATE INDEX IF NOT EXISTS ix_rubrica_consumo_aplicable
    ON criterios_consumo(estado, validada, vigente_desde, vigente_hasta);
