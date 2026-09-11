-- Baseline limpio NutriPredict — seeds mínimos (repeatable, idempotente).
-- Solo catálogos imprescindibles para arrancar:
--   unidades_medida ............ códigos ISO/uso (el bootstrap Java también los asegura)
--   procedimientos_analisis .... MANUAL + SOFTWARE_IA (requeridos para iniciar análisis)
-- Instrumentos PCC, rúbricas PCS/ground truth y reglas nutricionales NO se
-- inventan aquí: PENDIENTE_CONTENIDO_VALIDADO (cargar vía admin/endpoints).

INSERT INTO unidades_medida (codigo, nombre, activa)
VALUES
    ('MG', 'Miligramo', TRUE),
    ('G', 'Gramo', TRUE),
    ('MCG', 'Microgramo', TRUE),
    ('ML', 'Mililitro', TRUE),
    ('L', 'Litro', TRUE),
    ('CAPSULA', 'Cápsula', TRUE),
    ('TABLETA', 'Tableta', TRUE),
    ('SCOOP', 'Scoop', TRUE),
    ('PORCION', 'Porción', TRUE),
    ('UNIDAD', 'Unidad', TRUE)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO procedimientos_analisis (codigo, "version", tipo, nombre, activo)
SELECT 'MANUAL', 1, 'MANUAL', 'Análisis manual', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM procedimientos_analisis WHERE codigo = 'MANUAL' AND "version" = 1);

INSERT INTO procedimientos_analisis (codigo, "version", tipo, nombre, activo)
SELECT 'SOFTWARE_IA', 1, 'SOFTWARE_IA', 'Análisis mediante modelo predictivo', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM procedimientos_analisis WHERE codigo = 'SOFTWARE_IA' AND "version" = 1);
