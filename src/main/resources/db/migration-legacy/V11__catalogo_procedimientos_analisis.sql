INSERT INTO procedimientos_analisis (codigo, version, tipo, nombre, activo)
SELECT 'MANUAL', 1, 'MANUAL', 'Análisis manual', TRUE
WHERE NOT EXISTS (SELECT 1 FROM procedimientos_analisis WHERE codigo = 'MANUAL' AND version = 1);

INSERT INTO procedimientos_analisis (codigo, version, tipo, nombre, activo)
SELECT 'SOFTWARE_IA', 1, 'SOFTWARE_IA', 'Análisis mediante modelo predictivo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM procedimientos_analisis WHERE codigo = 'SOFTWARE_IA' AND version = 1);
