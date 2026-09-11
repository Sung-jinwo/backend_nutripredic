ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS estado VARCHAR(20);
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS validada BOOLEAN;
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS fuente_referencia VARCHAR(300);
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS version_referencia VARCHAR(100);
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS vigente_desde DATE;
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS vigente_hasta DATE;
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS peso_minimo_kg NUMERIC(6,2);
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS peso_maximo_kg NUMERIC(6,2);
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS altura_minima_cm NUMERIC(6,2);
ALTER TABLE reglas_requerimiento_nutricional ADD COLUMN IF NOT EXISTS altura_maxima_cm NUMERIC(6,2);
UPDATE reglas_requerimiento_nutricional SET fuente_referencia = fuente, version_referencia = version_fuente,
    estado = CASE WHEN activa THEN 'ACTIVA' ELSE 'INACTIVA' END, validada = FALSE
WHERE fuente_referencia IS NULL;
