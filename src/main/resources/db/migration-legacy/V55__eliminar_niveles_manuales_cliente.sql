-- V55: eliminar niveles manuales de Cliente + vigencia de historial de perfil.
--
-- 1. Cliente.nivel_conocimiento / nivel_consumo / ultima_evaluacion:
--    los niveles manuales se eliminan. PCC oficial vive en resultados_tests y
--    PCS oficial en evaluaciones_consumo (rubrica configurable).
-- 2. historial_perfiles_cliente.vigente_hasta: cierra el rango de aplicabilidad
--    de cada snapshot ([fecha_desde, vigente_hasta]; NULL = vigente).
-- 3. dias_entrenamiento_semana = 0 era el marcador legacy de "sin actividad";
--    el contrato nuevo usa NULL (ver §2: false -> dias/tipo nulos).

ALTER TABLE IF EXISTS clientes
    DROP COLUMN IF EXISTS nivel_conocimiento;

ALTER TABLE IF EXISTS clientes
    DROP COLUMN IF EXISTS nivel_consumo;

ALTER TABLE IF EXISTS clientes
    DROP COLUMN IF EXISTS ultima_evaluacion;

ALTER TABLE IF EXISTS historial_perfiles_cliente
    ADD COLUMN IF NOT EXISTS vigente_hasta DATE;

ALTER TABLE IF EXISTS clientes
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP;

UPDATE clientes
    SET dias_entrenamiento_semana = NULL
    WHERE dias_entrenamiento_semana = 0;
