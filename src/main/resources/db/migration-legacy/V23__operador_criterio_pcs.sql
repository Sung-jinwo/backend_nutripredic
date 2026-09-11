ALTER TABLE reglas_criterio_consumo
    ADD COLUMN IF NOT EXISTS cantidad_referencia_hasta NUMERIC(18,6);
