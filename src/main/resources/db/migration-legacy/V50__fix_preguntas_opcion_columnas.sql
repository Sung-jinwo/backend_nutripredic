-- Corrige duplicación de columnas opcion* creada por ddl-auto antes de explicitar name
ALTER TABLE preguntas_conocimiento_ia DROP COLUMN IF EXISTS opciona;
ALTER TABLE preguntas_conocimiento_ia DROP COLUMN IF EXISTS opcionb;
ALTER TABLE preguntas_conocimiento_ia DROP COLUMN IF EXISTS opcionc;
ALTER TABLE preguntas_conocimiento_ia DROP COLUMN IF EXISTS opciond;
