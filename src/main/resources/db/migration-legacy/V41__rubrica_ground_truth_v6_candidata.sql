ALTER TABLE rubricas_perfil_habitos
    ALTER COLUMN version TYPE NUMERIC(5,2) USING version::NUMERIC(5,2);

ALTER TABLE evaluaciones_perfil_habitos
    ADD COLUMN IF NOT EXISTS puntaje_maximo_calculable NUMERIC(6,2),
    ADD COLUMN IF NOT EXISTS cobertura_calculable NUMERIC(6,2),
    ADD COLUMN IF NOT EXISTS motivo_no_valida VARCHAR(1000);

CREATE TABLE criterios_rubrica_perfil_habitos (
    id BIGSERIAL PRIMARY KEY,
    rubrica_id BIGINT NOT NULL REFERENCES rubricas_perfil_habitos(id),
    dimension_id BIGINT NOT NULL REFERENCES dimensiones_rubrica_perfil_habitos(id),
    codigo VARCHAR(80) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo_evaluacion VARCHAR(40) NOT NULL,
    componente VARCHAR(80),
    fuente_datos VARCHAR(80),
    puntos_maximos NUMERIC(6,2) NOT NULL,
    parametros_json VARCHAR(4000),
    adherencia7d_json VARCHAR(2000),
    fuente VARCHAR(1000),
    organismo_autor VARCHAR(255),
    version_anio VARCHAR(80),
    referencia VARCHAR(1000),
    tipo_fuente VARCHAR(40) NOT NULL,
    orden INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_criterio_rubrica_perfil_codigo UNIQUE (rubrica_id, codigo),
    CONSTRAINT uq_criterio_rubrica_perfil_orden UNIQUE (rubrica_id, dimension_id, orden),
    CONSTRAINT ck_criterio_rubrica_perfil_puntos CHECK (puntos_maximos >= 0 AND puntos_maximos <= 100)
);

CREATE TABLE resultados_dimension_perfil_habitos (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id BIGINT NOT NULL REFERENCES evaluaciones_perfil_habitos(id),
    dimension_id BIGINT NOT NULL REFERENCES dimensiones_rubrica_perfil_habitos(id),
    puntos_obtenidos NUMERIC(6,2) NOT NULL,
    puntos_maximos_calculables NUMERIC(6,2) NOT NULL,
    cobertura_calculable NUMERIC(6,2) NOT NULL,
    criterios_no_calculables VARCHAR(2000),
    observacion VARCHAR(2000),
    CONSTRAINT uq_resultado_dimension_perfil UNIQUE (evaluacion_id, dimension_id)
);

CREATE TABLE resultados_criterio_perfil_habitos (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id BIGINT NOT NULL REFERENCES evaluaciones_perfil_habitos(id),
    criterio_id BIGINT NOT NULL REFERENCES criterios_rubrica_perfil_habitos(id),
    estado VARCHAR(40) NOT NULL,
    valor_observado NUMERIC(12,4),
    puntos_obtenidos NUMERIC(6,2),
    puntos_maximos NUMERIC(6,2),
    referencia_aplicada VARCHAR(1000),
    motivo_no_calculable VARCHAR(1000),
    observacion VARCHAR(2000),
    CONSTRAINT uq_resultado_criterio_perfil UNIQUE (evaluacion_id, criterio_id)
);

CREATE INDEX ix_criterio_rubrica_perfil_dimension
    ON criterios_rubrica_perfil_habitos (rubrica_id, dimension_id, orden);

CREATE INDEX ix_resultado_dimension_perfil_eval
    ON resultados_dimension_perfil_habitos (evaluacion_id);

CREATE INDEX ix_resultado_criterio_perfil_eval
    ON resultados_criterio_perfil_habitos (evaluacion_id);

INSERT INTO rubricas_perfil_habitos (
    codigo, version, nombre, descripcion, estado, observacion_validacion
)
VALUES (
    'RUBRICA_PERFIL_HABITOS_V1',
    0.90,
    'Rubrica candidata de perfil de habitos V6',
    'Version candidata 0.9 para preparar ground truth real V6. No validada; no usar para entrenamiento hasta activacion, validacion y vigencia formal.',
    'BORRADOR',
    'Pendiente de validacion experta. La cobertura minima candidata es 70%.'
)
ON CONFLICT (codigo, version) DO NOTHING;

WITH r AS (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 0.90
)
INSERT INTO dimensiones_rubrica_perfil_habitos (
    rubrica_id, codigo, nombre, descripcion, puntaje_minimo, puntaje_maximo, peso_porcentual, orden
)
SELECT r.id, v.codigo, v.nombre, v.descripcion, 0, v.peso, v.peso, v.orden
FROM r
CROSS JOIN (
    VALUES
        ('ADECUACION_NUTRICIONAL', 'Adecuacion nutricional', 'Fibra, sodio, distribucion energetica, proteina y energia cuando exista requerimiento referenciado.', 30.00, 1),
        ('CALIDAD_PATRON_ALIMENTARIO', 'Calidad del patron alimentario', 'Frutas/verduras, preparaciones caseras, procesamiento y diversidad. Algunos criterios permanecen no calculables o manuales.', 20.00, 2),
        ('CONSUMO_AGUA', 'Consumo de agua', 'Agua pura registrada en habitos; no representa hidratacion total.', 15.00, 3),
        ('PATRON_SUPLEMENTACION', 'Patron de suplementacion', 'Criterios de juicio especialista sobre complementariedad, coherencia y referencia conocida.', 15.00, 4),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'Consumo real de suplementos', 'Cafeina objetiva, creatina descriptiva y macronutrientes suplementarios en contexto de dieta total.', 20.00, 5)
) AS v(codigo, nombre, descripcion, peso, orden)
ON CONFLICT (rubrica_id, codigo) DO NOTHING;

WITH r AS (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 0.90
)
INSERT INTO criterios_clasificacion_perfil (
    rubrica_id, clasificacion, limite_inferior, limite_superior, incluye_inferior, incluye_superior, orden
)
SELECT r.id, v.clasificacion, v.inferior, v.superior, TRUE, v.incluye_superior, v.orden
FROM r
CROSS JOIN (
    VALUES
        ('CRITICO', 0.00, 50.00, FALSE, 1),
        ('MEJORABLE', 50.00, 80.00, FALSE, 2),
        ('ADECUADO', 80.00, 100.00, TRUE, 3)
) AS v(clasificacion, inferior, superior, incluye_superior, orden)
ON CONFLICT (rubrica_id, clasificacion) DO NOTHING;

WITH r AS (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 0.90
),
d AS (
    SELECT d.id, d.rubrica_id, d.codigo
    FROM dimensiones_rubrica_perfil_habitos d
    JOIN r ON r.id = d.rubrica_id
),
tabla AS (
    SELECT '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}'::VARCHAR AS adherencia
)
INSERT INTO criterios_rubrica_perfil_habitos (
    rubrica_id, dimension_id, codigo, nombre, tipo_evaluacion, componente, fuente_datos,
    puntos_maximos, parametros_json, adherencia7d_json, fuente, organismo_autor,
    version_anio, referencia, tipo_fuente, orden, activo
)
SELECT r.id, d.id, v.codigo, v.nombre, v.tipo_evaluacion, v.componente, v.fuente_datos,
       v.puntos, v.parametros, v.adherencia, v.fuente, v.organismo,
       v.version_anio, v.referencia, v.tipo_fuente, v.orden, TRUE
FROM r
JOIN (
    VALUES
        ('ADECUACION_NUTRICIONAL', 'FIBRA_25G_DIA', 'Fibra >= 25 g/dia', 'AUTOMATICO', 'FIBRA', 'ALIMENTACION', 5.00, '{"minG":25}', NULL, 'National Academies DRI macronutrient references / criterio candidato', 'National Academies', 'DRI', 'Referencia candidata de fibra para adultos; pendiente de validacion local.', 'REFERENCIA_EXTERNA', 1),
        ('ADECUACION_NUTRICIONAL', 'SODIO_MENOR_2000MG_DIA', 'Sodio < 2000 mg/dia', 'AUTOMATICO', 'SODIO', 'ALIMENTACION', 5.00, '{"maxMg":2000}', NULL, 'WHO sodium guideline / criterio candidato', 'World Health Organization', '2012', 'Referencia candidata de sodio; pendiente de validacion local.', 'REFERENCIA_EXTERNA', 2),
        ('ADECUACION_NUTRICIONAL', 'CARBOHIDRATOS_45_75_ENERGIA', 'Carbohidratos 45-75% energia', 'AUTOMATICO', 'CARBOHIDRATOS', 'ALIMENTACION', 5.00, '{"porcentajeMin":45,"porcentajeMax":75,"factorKcalG":4}', NULL, 'AMDR candidate range', 'National Academies', 'DRI', 'Rango candidato por porcentaje energetico; pendiente de validacion local.', 'REFERENCIA_EXTERNA', 3),
        ('ADECUACION_NUTRICIONAL', 'GRASAS_15_30_ENERGIA', 'Grasas 15-30% energia', 'AUTOMATICO', 'GRASAS', 'ALIMENTACION', 5.00, '{"porcentajeMin":15,"porcentajeMax":30,"factorKcalG":9}', NULL, 'AMDR candidate range', 'National Academies', 'DRI', 'Rango candidato por porcentaje energetico; pendiente de validacion local.', 'REFERENCIA_EXTERNA', 4),
        ('ADECUACION_NUTRICIONAL', 'PROTEINA_083_GKG_DIA', 'Proteina >= 0.83 g/kg/dia', 'AUTOMATICO', 'PROTEINA', 'ALIMENTACION', 5.00, '{"minGKgDia":0.83}', NULL, 'Protein requirement candidate', 'WHO/FAO/UNU', '2007', 'Referencia candidata de proteina en g/kg/dia; no penaliza valores superiores.', 'REFERENCIA_EXTERNA', 5),
        ('ADECUACION_NUTRICIONAL', 'ENERGIA_REQUERIMIENTO_REFERENCIADO', 'Energia contra requerimiento referenciado', 'AUTOMATICO', 'ENERGIA', 'TOTAL_DIETA', 5.00, '{}', NULL, 'RequerimientoNutricionalService', 'NutriPredict', 'motor-v1', 'Solo calculable si existe requerimiento energetico valido y politica de adecuacion.', 'REGLA_OPERACIONAL_PROPUESTA', 6),
        ('CALIDAD_PATRON_ALIMENTARIO', 'FRUTAS_VERDURAS_400G_DIA', 'Frutas y verduras >= 400 g/dia', 'AUTOMATICO', 'FRUTAS_VERDURAS', 'ALIMENTACION', 5.00, '{"minG":400}', NULL, 'WHO healthy diet factsheet / criterio candidato', 'World Health Organization', '2020', 'Solo calculable si catalogo clasifica frutas y verduras de forma fiable.', 'REFERENCIA_EXTERNA', 1),
        ('CALIDAD_PATRON_ALIMENTARIO', 'PREPARACIONES_CASERAS', 'Preparaciones caseras', 'AUTOMATICO', 'COMIDAS_COCINADAS', 'HABITOS', 5.00, '{"minComidasCocinadasDia":1}', NULL, 'Regla operacional candidata', 'NutriPredict', '0.9', 'Usa comidasCocinadas como evidencia; puntuacion pendiente de validacion.', 'REGLA_OPERACIONAL_PROPUESTA', 2),
        ('CALIDAD_PATRON_ALIMENTARIO', 'NATURALES_PROCESADOS', 'Alimentos naturales/procesados', 'AUTOMATICO', 'PROCESAMIENTO', 'ALIMENTACION', 5.00, '{}', NULL, 'Regla operacional pendiente', 'NutriPredict', '0.9', 'No calculable hasta que catalogo tenga clasificacion fiable.', 'REGLA_OPERACIONAL_PROPUESTA', 3),
        ('CALIDAD_PATRON_ALIMENTARIO', 'DIVERSIDAD_MANUAL', 'Diversidad alimentaria', 'MANUAL_ESPECIALISTA', 'DIVERSIDAD', 'ESPECIALISTA', 5.00, '{}', NULL, 'Juicio especialista', 'Especialista NutriPredict', '0.9', 'No se inventa indice automatico inicial.', 'JUICIO_ESPECIALISTA', 4),
        ('CONSUMO_AGUA', 'AGUA_1000ML_DIA', 'Agua pura >= 1000 ml/dia', 'AUTOMATICO', 'AGUA', 'HABITOS', 15.00, '{"minMl":1000,"unidadRegistro":"LITROS"}', NULL, 'Referencia operacional candidata', 'NutriPredict', '0.9', 'Usa RegistroHabito.consumoAgua como agua pura registrada, no hidratacion total.', 'REGLA_OPERACIONAL_PROPUESTA', 1),
        ('PATRON_SUPLEMENTACION', 'COMPLEMENTARIEDAD_MANUAL', 'Complementariedad alimentacion/suplementacion', 'MANUAL_ESPECIALISTA', 'SUPLEMENTACION', 'ESPECIALISTA', 5.00, '{}', NULL, 'Juicio especialista', 'Especialista NutriPredict', '0.9', 'Evaluacion manual.', 'JUICIO_ESPECIALISTA', 1),
        ('PATRON_SUPLEMENTACION', 'COHERENCIA_HABITO_CONSUMO_MANUAL', 'Coherencia entre habito declarado y consumo real', 'MANUAL_ESPECIALISTA', 'SUPLEMENTACION', 'ESPECIALISTA', 5.00, '{}', NULL, 'Juicio especialista', 'Especialista NutriPredict', '0.9', 'Evaluacion manual.', 'JUICIO_ESPECIALISTA', 2),
        ('PATRON_SUPLEMENTACION', 'REFERENCIA_CONOCIDA_MANUAL', 'Uso con referencia conocida', 'MANUAL_ESPECIALISTA', 'SUPLEMENTACION', 'ESPECIALISTA', 5.00, '{}', NULL, 'Juicio especialista', 'Especialista NutriPredict', '0.9', 'Evaluacion manual.', 'JUICIO_ESPECIALISTA', 3),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'CAFEINA_SUPLEMENTARIA_400MG_DIA', 'Cafeina suplementaria <= 400 mg/dia', 'AUTOMATICO', 'CAFEINA', 'SUPLEMENTACION', 5.00, '{"maxMg":400}', NULL, 'Caffeine safety candidate threshold', 'FDA', 'current', 'Senal objetiva de exceso si supera 400 mg/dia.', 'REFERENCIA_EXTERNA', 1),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'CREATINA_DESCRIPTIVA', 'Creatina descriptiva', 'DESCRIPTIVO', 'CREATINA', 'SUPLEMENTACION', 0.00, '{}', NULL, 'Regla operacional candidata', 'NutriPredict', '0.9', 'No penaliza automaticamente >5 g; dato para revision.', 'REGLA_OPERACIONAL_PROPUESTA', 2),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'PROTEINA_SUPLEMENTARIA_CONTEXTO', 'Proteina suplementaria en contexto de proteina total', 'AUTOMATICO', 'PROTEINA_SUPLEMENTARIA', 'TOTAL_DIETA', 5.00, '{}', NULL, 'Regla operacional pendiente', 'NutriPredict', '0.9', 'No penaliza por existir; requiere contexto de proteina total.', 'REGLA_OPERACIONAL_PROPUESTA', 3),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'CARBOHIDRATOS_SUPLEMENTARIOS_TOTAL_DIETA', 'Carbohidratos suplementarios respecto a dieta total', 'AUTOMATICO', 'CARBOHIDRATOS_SUPLEMENTARIOS', 'TOTAL_DIETA', 5.00, '{}', NULL, 'Regla operacional pendiente', 'NutriPredict', '0.9', 'Calculable solo con referencia aplicable de dieta total.', 'REGLA_OPERACIONAL_PROPUESTA', 4),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'GRASAS_SUPLEMENTARIAS_TOTAL_DIETA', 'Grasas suplementarias respecto a dieta total', 'AUTOMATICO', 'GRASAS_SUPLEMENTARIAS', 'TOTAL_DIETA', 5.00, '{}', NULL, 'Regla operacional pendiente', 'NutriPredict', '0.9', 'Calculable solo con referencia aplicable de dieta total.', 'REGLA_OPERACIONAL_PROPUESTA', 5),
        ('CONSUMO_REAL_SUPLEMENTOS_V6', 'AZUCAR_LIBRE_NO_CALCULABLE', 'Azucar libre/anadida', 'AUTOMATICO', 'AZUCAR_LIBRE', 'ALIMENTACION', 0.00, '{}', NULL, 'WHO sugars guideline; no aplicable a azucar total', 'World Health Organization', '2015', 'No comparar azucar total contra limite OMS de azucares libres.', 'REFERENCIA_EXTERNA', 6)
) AS v(dimension_codigo, codigo, nombre, tipo_evaluacion, componente, fuente_datos, puntos, parametros, adherencia, fuente, organismo, version_anio, referencia, tipo_fuente, orden)
    ON TRUE
JOIN d ON d.codigo = v.dimension_codigo
ON CONFLICT (rubrica_id, codigo) DO NOTHING;

UPDATE criterios_rubrica_perfil_habitos
SET adherencia7d_json = '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}'
WHERE rubrica_id = (
    SELECT id FROM rubricas_perfil_habitos
    WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 0.90
)
AND tipo_evaluacion = 'AUTOMATICO'
AND puntos_maximos > 0
AND adherencia7d_json IS NULL;
