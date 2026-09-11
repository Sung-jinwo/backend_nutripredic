-- V12: corrige desfases entidad <-> esquema que rompen ddl-auto=validate.
-- No modifica migraciones ya ejecutadas (V3/V5/V6/V7/V10); todo es idempotente.

-- A. Respaldo suplementos_cliente (V7+V10): si una BD marcó V7/V10 como
-- aplicadas sin crear las columnas (baseline manual, restore parcial),
-- esto las garantiza antes del validate de Hibernate.
ALTER TABLE suplementos_cliente
  ADD COLUMN IF NOT EXISTS componentes_declarados VARCHAR(1000),
  ADD COLUMN IF NOT EXISTS energia_kcal_por_toma NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS proteina_g_por_toma NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS carbohidratos_g_por_toma NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS grasas_g_por_toma NUMERIC(12,4),
  ADD COLUMN IF NOT EXISTS nombre_declarado VARCHAR(255);

-- Rellena nombre_declarado en BDs que perdieron el backfill de V10.
UPDATE suplementos_cliente sc
SET nombre_declarado = s.nombre
FROM suplementos_catalogo s
WHERE sc.suplemento_id = s.id
  AND sc.nombre_declarado IS NULL;

-- B. Renombra columnas con typo en V5/V6 a los nombres que esperan las
-- entidades (snake_case). RENAME conserva los datos. Cada bloque solo
-- actúa si existe la columna vieja y falta la nueva: re-ejecutable.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opciona')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opcion_a') THEN
    ALTER TABLE preguntas_conocimiento RENAME COLUMN opciona TO opcion_a;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opcionb')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opcion_b') THEN
    ALTER TABLE preguntas_conocimiento RENAME COLUMN opcionb TO opcion_b;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opcionc')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opcion_c') THEN
    ALTER TABLE preguntas_conocimiento RENAME COLUMN opcionc TO opcion_c;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opciond')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'preguntas_conocimiento' AND column_name = 'opcion_d') THEN
    ALTER TABLE preguntas_conocimiento RENAME COLUMN opciond TO opcion_d;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reglas_requerimiento_nutricional' AND column_name = 'proteina_objetivog')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reglas_requerimiento_nutricional' AND column_name = 'proteina_objetivo_g') THEN
    ALTER TABLE reglas_requerimiento_nutricional RENAME COLUMN proteina_objetivog TO proteina_objetivo_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reglas_requerimiento_nutricional' AND column_name = 'carbohidratos_objetivog')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reglas_requerimiento_nutricional' AND column_name = 'carbohidratos_objetivo_g') THEN
    ALTER TABLE reglas_requerimiento_nutricional RENAME COLUMN carbohidratos_objetivog TO carbohidratos_objetivo_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reglas_requerimiento_nutricional' AND column_name = 'grasas_objetivog')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reglas_requerimiento_nutricional' AND column_name = 'grasas_objetivo_g') THEN
    ALTER TABLE reglas_requerimiento_nutricional RENAME COLUMN grasas_objetivog TO grasas_objetivo_g;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'proteina_ming')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'proteina_min_g') THEN
    ALTER TABLE planes_diarios RENAME COLUMN proteina_ming TO proteina_min_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'proteina_maxg')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'proteina_max_g') THEN
    ALTER TABLE planes_diarios RENAME COLUMN proteina_maxg TO proteina_max_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'carbohidratos_ming')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'carbohidratos_min_g') THEN
    ALTER TABLE planes_diarios RENAME COLUMN carbohidratos_ming TO carbohidratos_min_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'carbohidratos_maxg')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'carbohidratos_max_g') THEN
    ALTER TABLE planes_diarios RENAME COLUMN carbohidratos_maxg TO carbohidratos_max_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'grasas_ming')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'grasas_min_g') THEN
    ALTER TABLE planes_diarios RENAME COLUMN grasas_ming TO grasas_min_g;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'grasas_maxg')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'planes_diarios' AND column_name = 'grasas_max_g') THEN
    ALTER TABLE planes_diarios RENAME COLUMN grasas_maxg TO grasas_max_g;
  END IF;
END $$;
