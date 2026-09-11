-- Módulos sin consumidor en el frontend y sustituidos por estructuras activas.
-- No se usa CASCADE: si aparece una dependencia nueva, Flyway debe fallar de forma segura.

-- GPAQ fue retirado del flujo. También se eliminan únicamente sus evaluaciones padre.
WITH evaluaciones_retiradas AS (
    DELETE FROM evaluaciones_gpaq
    RETURNING evaluacion_actividad_id
)
DELETE FROM evaluaciones_actividad_fisica
WHERE id IN (SELECT evaluacion_actividad_id FROM evaluaciones_retiradas);

DROP TABLE IF EXISTS evaluaciones_gpaq;

-- La composición estructurada reemplaza el catálogo paralelo de ingredientes.
DROP TABLE IF EXISTS suplementos_ingredientes;

-- Las plantillas no tienen registros ni consumidor en las vistas actuales.
DROP TABLE IF EXISTS plantillas_comida_detalles;
DROP TABLE IF EXISTS plantillas_comida;

-- El reintento conserva el id de origen para auditoría, pero sin FK autorreferente.
-- Así no existe un ciclo de dependencias en el esquema.
ALTER TABLE sesiones_conocimiento_ia
    DROP CONSTRAINT IF EXISTS sesiones_conocimiento_ia_reintento_de_sesion_id_fkey;
