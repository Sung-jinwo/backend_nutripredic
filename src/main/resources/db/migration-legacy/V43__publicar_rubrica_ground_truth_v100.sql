-- Publicar RUBRICA_PERFIL_HABITOS_V1 version 1.00 (ACTIVA, VALIDADA, VIGENTE)
-- Validacion tecnico-documental basada en fuentes externas referenciadas.
-- La 0.90 permanece como BORRADOR historico.

-- 1. Anadir columnas de metadata de validacion si no existen
ALTER TABLE rubricas_perfil_habitos
    ADD COLUMN IF NOT EXISTS tipo_validacion VARCHAR(80),
    ADD COLUMN IF NOT EXISTS fuente_validacion VARCHAR(4000),
    ADD COLUMN IF NOT EXISTS version_fuente VARCHAR(80);

-- 2. Desactivar cualquier version ACTIVA anterior del mismo codigo
UPDATE rubricas_perfil_habitos
SET estado = 'INACTIVA'
WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND estado = 'ACTIVA';

-- 3. Insertar version 1.00 ACTIVA y VALIDADA (validacion tecnico-documental)
INSERT INTO rubricas_perfil_habitos (
    codigo, version, nombre, descripcion, estado,
    validado_por, validado_en, vigente_desde, observacion_validacion,
    tipo_validacion, fuente_validacion, version_fuente
)
VALUES (
    'RUBRICA_PERFIL_HABITOS_V1',
    1.00,
    'Rubrica ground truth de perfil de habitos V6',
    'Version 1.00 con validacion tecnico-documental para ground truth real V6. Fuentes: OMS, MINSA/INS, EFSA, NIH-ODS. Ponderaciones D1-D5, tabla de adherencia semanal, cobertura minima 70% y cortes 80/50 son REGLA_OPERACIONAL_DEL_INSTRUMENTO.',
    'ACTIVA',
    NULL,                                      -- validado_por: sin comite ni acta inventada
    NOW(),                                     -- validado_en: fecha de publicacion tecnica
    NOW(),                                     -- vigente_desde: fecha real de vigencia
    'Validacion tecnico-documental. No hay comite ni acta de expertos. La validez se deriva de las fuentes externas citadas en fuente_validacion. Version fuente: RUBRICA_PERFIL_HABITOS_V1-1.00. Ponderaciones 30/20/15/15/20, adherencia semanal, cobertura 70%, cortes 80/50 = REGLA_OPERACIONAL_DEL_INSTRUMENTO.',
    'VALIDACION_TECNICO_DOCUMENTAL',
    'OMS Healthy Diet 2025; WHO carbohydrate/fat guidelines 2023; WHO sodium guidance; MINSA/INS Guías Alimentarias para la Población Peruana; EFSA Protein DRV; EFSA Caffeine Safety; NIH-ODS Exercise and Athletic Performance',
    'RUBRICA_PERFIL_HABITOS_V1-1.00'
)
ON CONFLICT (codigo, version) DO NOTHING;

-- 4. Insertar dimensiones V1.00 (pesos = REGLA_OPERACIONAL_DEL_INSTRUMENTO)
-- Limpiar dimensiones previas de la rubrica 1.00 si existen (reintentos fallidos)
DELETE FROM dimensiones_rubrica_perfil_habitos
WHERE rubrica_id = (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 1.00
);

WITH r1 AS (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 1.00
)
INSERT INTO dimensiones_rubrica_perfil_habitos (
    rubrica_id, codigo, nombre, descripcion,
    puntaje_minimo, puntaje_maximo, peso_porcentual, orden
)
SELECT r1.id, v.codigo, v.nombre, v.descripcion, 0, v.peso, v.peso, v.orden
FROM r1
CROSS JOIN (
    VALUES
        ('ADECUACION_NUTRICIONAL', 'Adecuacion nutricional', 'Fibra, sodio, distribucion energetica, proteina. Peso 30% = REGLA_OPERACIONAL_DEL_INSTRUMENTO.', 30.00, 1),
        ('CALIDAD_PATRON_ALIMENTARIO', 'Calidad del patron alimentario', 'Frutas/verduras, preparaciones caseras, procesamiento, diversidad. Peso 20% = REGLA_OPERACIONAL_DEL_INSTRUMENTO.', 20.00, 2),
        ('CONSUMO_AGUA', 'Consumo de agua', 'Agua pura registrada en habitos; referencia MINSA 6-8 vasos/dia. Peso 15% = REGLA_OPERACIONAL_DEL_INSTRUMENTO.', 15.00, 3),
        ('PATRON_SUPLEMENTACION', 'Patron de suplementacion', 'Complementariedad, coherencia, referencia conocida (manual). Peso 15% = REGLA_OPERACIONAL_DEL_INSTRUMENTO.', 15.00, 4),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'Consumo real de suplementos', 'Cafeina objetiva, creatina descriptiva, proteinas suplementarias en contexto. Peso 20% = REGLA_OPERACIONAL_DEL_INSTRUMENTO.', 20.00, 5)
) AS v(codigo, nombre, descripcion, peso, orden);

-- 5. Insertar criterios de clasificacion (cortes 80/50 = REGLA_OPERACIONAL_DEL_INSTRUMENTO)
DELETE FROM criterios_clasificacion_perfil
WHERE rubrica_id = (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 1.00
);

WITH r1 AS (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 1.00
)
INSERT INTO criterios_clasificacion_perfil (
    rubrica_id, clasificacion, limite_inferior, limite_superior,
    incluye_inferior, incluye_superior, orden
)
SELECT r1.id, v.clasificacion, v.inferior, v.superior, v.incluye_inferior, v.incluye_superior, v.orden
FROM r1
CROSS JOIN (
    VALUES
        ('CRITICO', 0.00, 50.00, FALSE, FALSE, 1),
        ('MEJORABLE', 50.00, 80.00, TRUE, FALSE, 2),
        ('ADECUADO', 80.00, 100.00, TRUE, TRUE, 3)
) AS v(clasificacion, inferior, superior, incluye_inferior, incluye_superior, orden);

-- 6. Insertar criterios de evaluacion V1.00 (versiones corregidas segun validacion tecnico-documental)
DELETE FROM criterios_rubrica_perfil_habitos
WHERE rubrica_id = (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 1.00
);

WITH r1 AS (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 1.00
),
d1 AS (
    SELECT d.id, d.codigo FROM dimensiones_rubrica_perfil_habitos d WHERE d.rubrica_id = (SELECT id FROM r1)
)
INSERT INTO criterios_rubrica_perfil_habitos (
    rubrica_id, dimension_id, codigo, nombre, tipo_evaluacion, componente,
    fuente_datos, puntos_maximos, parametros_json, adherencia7d_json,
    fuente, organismo_autor, version_anio, referencia, tipo_fuente, orden, activo
)
SELECT r1.id, d1.id, v.codigo, v.nombre, v.tipo_evaluacion, v.componente,
       v.fuente_datos, v.puntos, v.parametros, v.adherencia, v.fuente, v.organismo,
       v.version_anio, v.referencia, v.tipo_fuente, v.orden, TRUE
FROM r1
CROSS JOIN (
    VALUES
    -- D1: ADECUACION_NUTRICIONAL (peso 30%)
    ('ADECUACION_NUTRICIONAL', 'FIBRA_25G_DIA', 'Fibra >= 25 g/dia', 'AUTOMATICO', 'FIBRA', 'ALIMENTACION', 5.00,
     '{"minG":25}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'OMS Healthy Diet 2025 / WHO fiber guideline', 'World Health Organization', '2025',
     'Referencia OMS: >=25 g/dia fibra dietaria. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso criterio 5/30.', 'REFERENCIA_EXTERNA', 1),

    ('ADECUACION_NUTRICIONAL', 'SODIO_MENOR_2000MG_DIA', 'Sodio < 2000 mg/dia', 'AUTOMATICO', 'SODIO', 'ALIMENTACION', 5.00,
     '{"maxMg":2000}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'WHO sodium guidance', 'World Health Organization', '2012/2023',
     'OMS: adultos <2000 mg/dia sodio. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso criterio 5/30.', 'REFERENCIA_EXTERNA', 2),

    ('ADECUACION_NUTRICIONAL', 'CARBOHIDRATOS_45_75_ENERGIA', 'Carbohidratos 45-75% energia', 'AUTOMATICO', 'CARBOHIDRATOS', 'ALIMENTACION', 5.00,
     '{"porcentajeMin":45,"porcentajeMax":75,"factorKcalG":4}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'WHO carbohydrate guideline 2023', 'World Health Organization', '2023',
     'OMS: 45-75% energia total desde carbohidratos. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso criterio 5/30.', 'REFERENCIA_EXTERNA', 3),

    ('ADECUACION_NUTRICIONAL', 'GRASAS_30_ENERGIA', 'Grasas <=30% energia (referencia OMS)', 'AUTOMATICO', 'GRASAS', 'ALIMENTACION', 5.00,
     '{"porcentajeMax":30,"factorKcalG":9}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'WHO total fat guideline 2023', 'World Health Organization', '2023',
     'OMS: <=30% energia total desde grasas. No se establece 15% como limite inferior obligatorio sin referencia adicional. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso criterio 5/30.', 'REFERENCIA_EXTERNA', 4),

    ('ADECUACION_NUTRICIONAL', 'PROTEINA_083_GKG_DIA', 'Proteina >= 0.83 g/kg/dia sin techo', 'AUTOMATICO', 'PROTEINA', 'ALIMENTACION', 5.00,
     '{"minGKgDia":0.83}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'EFSA Protein DRV / WHO protein requirement', 'EFSA / WHO/FAO/UNU', '2012/2007',
     'EFSA/WHO: >=0.83 g/kg/dia proteina. Sin techo automatico. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso criterio 5/30.', 'REFERENCIA_EXTERNA', 5),

    ('ADECUACION_NUTRICIONAL', 'ENERGIA_NO_CALCULABLE', 'Energia sin requerimiento individual: NO_CALCULABLE', 'AUTOMATICO', 'ENERGIA', 'TOTAL_DIETA', 5.00,
     '{}', NULL,
     'Regla operacional', 'NutriPredict', '1.00',
     'NO_CALCULABLE mientras no exista requerimiento energetico individual validado. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso criterio 5/30.', 'REGLA_OPERACIONAL_DEL_INSTRUMENTO', 6),

    -- D2: CALIDAD_PATRON_ALIMENTARIO (peso 20%)
    ('CALIDAD_PATRON_ALIMENTARIO', 'FRUTAS_VERDURAS_400G_DIA', 'Frutas y verduras >= 400 g/dia', 'AUTOMATICO', 'FRUTAS_VERDURAS', 'ALIMENTACION', 5.00,
     '{"minG":400}', NULL,
     'OMS Healthy Diet 2025', 'World Health Organization', '2025',
     'OMS: >=400 g/dia frutas y verduras. NO_CALCULABLE mientras catalogo no clasifique fiablemente. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso criterio 5/20.', 'REFERENCIA_EXTERNA', 1),

    ('CALIDAD_PATRON_ALIMENTARIO', 'PREPARACIONES_CASERAS', 'Preparaciones caseras (comidas cocinadas)', 'AUTOMATICO', 'COMIDAS_COCINADAS', 'HABITOS', 5.00,
     '{"minComidasCocinadasDia":1}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'Regla operacional', 'NutriPredict', '1.00',
     'Usa comidasCocinadas como evidencia. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso criterio 5/20.', 'REGLA_OPERACIONAL_DEL_INSTRUMENTO', 2),

    ('CALIDAD_PATRON_ALIMENTARIO', 'NATURALES_PROCESADOS', 'Alimentos naturales/procesados', 'AUTOMATICO', 'PROCESAMIENTO', 'ALIMENTACION', 5.00,
     '{}', NULL,
     'Regla operacional', 'NutriPredict', '1.00',
     'NO_CALCULABLE hasta que catalogo tenga clasificacion fiable natural/procesado. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso criterio 5/20.', 'REGLA_OPERACIONAL_DEL_INSTRUMENTO', 3),

    ('CALIDAD_PATRON_ALIMENTARIO', 'DIVERSIDAD_MANUAL', 'Diversidad alimentaria', 'MANUAL_ESPECIALISTA', 'DIVERSIDAD', 'ESPECIALISTA', 5.00,
     '{}', NULL,
     'Juicio especialista', 'Especialista NutriPredict', '1.00',
     'Evaluacion manual. No se inventa indice automatico. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso criterio 5/20.', 'JUICIO_ESPECIALISTA', 4),

    -- D3: CONSUMO_AGUA (peso 15%)
    ('CONSUMO_AGUA', 'AGUA_6_8_VASOS_DIA', 'Agua pura: referencia MINSA 6-8 vasos/dia', 'AUTOMATICO', 'AGUA', 'HABITOS', 15.00,
     '{"referenciaVasos":"6-8 vasos/dia","vasosMinimos":6,"requiereEquivalenciaMlPorVasoDocumentada":true,"unidadRegistro":"LITROS"}', NULL,
     'MINSA/INS Guías Alimentarias para la Población Peruana', 'MINSA/INS', 'No especificada',
     'Referencia trazable: 6-8 vasos/dia. Sin equivalencia ml/vaso documentada y adoptada por el sistema, el criterio es NO_CALCULABLE. Se retira >=1000 ml como corte cientifico. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 15/15.', 'REFERENCIA_EXTERNA', 1),

    -- D4: PATRON_SUPLEMENTACION (peso 15%)
    ('PATRON_SUPLEMENTACION', 'COMPLEMENTARIEDAD_MANUAL', 'Complementariedad alimentacion/suplementacion', 'MANUAL_ESPECIALISTA', 'SUPLEMENTACION', 'ESPECIALISTA', 5.00,
     '{}', NULL,
     'Juicio especialista', 'Especialista NutriPredict', '1.00',
     'Evaluacion manual. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 5/15.', 'JUICIO_ESPECIALISTA', 1),

    ('PATRON_SUPLEMENTACION', 'COHERENCIA_HABITO_CONSUMO_MANUAL', 'Coherencia entre habito declarado y consumo real', 'MANUAL_ESPECIALISTA', 'SUPLEMENTACION', 'ESPECIALISTA', 5.00,
     '{}', NULL,
     'Juicio especialista', 'Especialista NutriPredict', '1.00',
     'Evaluacion manual. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 5/15.', 'JUICIO_ESPECIALISTA', 2),

    ('PATRON_SUPLEMENTACION', 'REFERENCIA_CONOCIDA_MANUAL', 'Uso con referencia conocida', 'MANUAL_ESPECIALISTA', 'SUPLEMENTACION', 'ESPECIALISTA', 5.00,
     '{}', NULL,
     'Juicio especialista', 'Especialista NutriPredict', '1.00',
     'Evaluacion manual. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 5/15.', 'JUICIO_ESPECIALISTA', 3),

    -- D5: CONSUMO_REAL_SUPLEMENTOS_V6 (peso 20%)
    ('CONSUMO_REAL_SUPLEMENTOS_V6', 'CAFEINA_SUPLEMENTARIA_400MG_DIA', 'Cafeina suplementaria: >400 mg/dia = SUPERA_UMBRAL_TOTAL_DESDE_SUPLEMENTOS; <=400 = SIN_EXCESO_DOCUMENTADO_DESDE_SUPLEMENTOS', 'AUTOMATICO', 'CAFEINA', 'SUPLEMENTACION', 5.00,
     '{"maxMg":400,"alcanceUmbral":"TODAS_LAS_FUENTES","fuenteObservada":"SOLO_SUPLEMENTOS"}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'EFSA Caffeine Safety', 'European Food Safety Authority', '2015',
     'EFSA: hasta 400 mg/dia considera todas las fuentes. >400 mg/dia solo desde suplementos = SUPERA_UMBRAL_TOTAL_DESDE_SUPLEMENTOS; <=400 mg/dia solo desde suplementos = SIN_EXCESO_DOCUMENTADO_DESDE_SUPLEMENTOS (no afirma seguridad total). REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso 5/20.', 'REFERENCIA_EXTERNA', 1),

    ('CONSUMO_REAL_SUPLEMENTOS_V6', 'CREATINA_DESCRIPTIVA', 'Creatina descriptiva (sin penalizacion automatica >5 g)', 'DESCRIPTIVO', 'CREATINA', 'SUPLEMENTACION', 0.00,
     '{}', NULL,
     'NIH-ODS Exercise and Athletic Performance', 'NIH Office of Dietary Supplements', 'Actual',
     'Registro descriptivo de creatina. No genera penalizacion automatica. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 0/20.', 'REFERENCIA_EXTERNA', 2),

    ('CONSUMO_REAL_SUPLEMENTOS_V6', 'PROTEINA_SUPLEMENTARIA_CONTEXTO', 'Proteina suplementaria en contexto de proteina total', 'AUTOMATICO', 'PROTEINA_SUPLEMENTARIA', 'TOTAL_DIETA', 5.00,
     '{}', '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
     'Regla operacional', 'NutriPredict', '1.00',
     'No penaliza por existir; requiere contexto de proteina total. REGLA_OPERACIONAL_DEL_INSTRUMENTO: tabla adherencia semanal, peso 5/20.', 'REGLA_OPERACIONAL_DEL_INSTRUMENTO', 3),

    ('CONSUMO_REAL_SUPLEMENTOS_V6', 'CARBOHIDRATOS_SUPLEMENTARIOS_TOTAL_DIETA', 'Carbohidratos suplementarios respecto a dieta total', 'AUTOMATICO', 'CARBOHIDRATOS_SUPLEMENTARIOS', 'TOTAL_DIETA', 5.00,
     '{}', NULL,
     'Regla operacional', 'NutriPredict', '1.00',
     'Calculable solo con referencia aplicable de dieta total. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 5/20.', 'REGLA_OPERACIONAL_DEL_INSTRUMENTO', 4),

    ('CONSUMO_REAL_SUPLEMENTOS_V6', 'GRASAS_SUPLEMENTARIAS_TOTAL_DIETA', 'Grasas suplementarias respecto a dieta total', 'AUTOMATICO', 'GRASAS_SUPLEMENTARIAS', 'TOTAL_DIETA', 5.00,
     '{}', NULL,
     'Regla operacional', 'NutriPredict', '1.00',
     'Calculable solo con referencia aplicable de dieta total. REGLA_OPERACIONAL_DEL_INSTRUMENTO: peso 5/20.', 'REGLA_OPERACIONAL_DEL_INSTRUMENTO', 5),

    ('CONSUMO_REAL_SUPLEMENTOS_V6', 'AZUCAR_TOTAL_NO_COMPARAR_LIBRE', 'Azucar total no se compara con limite OMS de azucares libres', 'AUTOMATICO', 'AZUCAR_LIBRE', 'ALIMENTACION', 0.00,
     '{}', NULL,
     'WHO sugars guideline', 'World Health Organization', '2015',
     'No comparar azucar total contra limite OMS de azucares libres/anadidos. Criterio informativo, peso 0. REGLA_OPERACIONAL_DEL_INSTRUMENTO.', 'REFERENCIA_EXTERNA', 6)
) AS v(dimension_codigo, codigo, nombre, tipo_evaluacion, componente, fuente_datos, puntos, parametros, adherencia, fuente, organismo, version_anio, referencia, tipo_fuente, orden)
JOIN d1 ON d1.codigo = v.dimension_codigo;

-- 7. Verificacion (comentario)
-- SELECT r.codigo, r.version, r.estado, r.tipo_validacion, r.fuente_validacion, r.version_fuente,
--        r.validado_por, r.validado_en, r.vigente_desde
-- FROM rubricas_perfil_habitos r
-- WHERE r.codigo = 'RUBRICA_PERFIL_HABITOS_V1';