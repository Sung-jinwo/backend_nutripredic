-- Ajuste trazable de la rúbrica candidata; V41 ya aplicada no se modifica.
-- No activa ni valida la rúbrica 0.90.

ALTER TABLE resultados_criterio_perfil_habitos
    ALTER COLUMN estado TYPE VARCHAR(80);

UPDATE rubricas_perfil_habitos
SET descripcion = 'Versión candidata 0.90 para preparar ground truth real V6. No validada; no usar para entrenamiento hasta activación, validación y vigencia formal. Las ponderaciones D1–D5, tabla de adherencia semanal, cobertura mínima 70% y cortes 80/50 son REGLA_OPERACIONAL_PROPUESTA de NutriPredict, no umbrales OMS/MINSA/EFSA.',
    observacion_validacion = 'BORRADOR. Ponderaciones D1–D5, tabla semanal, cobertura mínima 70% y cortes 80/50: REGLA_OPERACIONAL_PROPUESTA. Pendiente de validación experta.'
WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1' AND version = 0.90;

UPDATE dimensiones_rubrica_perfil_habitos d
SET descripcion = d.descripcion || ' Ponderación de esta dimensión: REGLA_OPERACIONAL_PROPUESTA de NutriPredict; no es un umbral de fuente externa.'
FROM rubricas_perfil_habitos r
WHERE d.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND d.descripcion NOT LIKE '%REGLA_OPERACIONAL_PROPUESTA%';

UPDATE criterios_rubrica_perfil_habitos c
SET adherencia7d_json = '{"0":0,"1":1,"2":2,"3":3,"4":3,"5":4,"6":5,"7":5}',
    referencia = COALESCE(c.referencia, '') || ' Tabla de adherencia semanal: REGLA_OPERACIONAL_PROPUESTA de NutriPredict.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.tipo_evaluacion = 'AUTOMATICO'
  AND c.puntos_maximos > 0
  AND c.referencia NOT LIKE '%Tabla de adherencia semanal: REGLA_OPERACIONAL_PROPUESTA%';

UPDATE criterios_rubrica_perfil_habitos c
SET codigo = 'AGUA_6_8_VASOS_DIA',
    nombre = 'Agua pura: referencia 6–8 vasos/día',
    parametros_json = '{"referenciaVasos":"6–8 vasos/día","vasosMinimos":6,"requiereEquivalenciaMlPorVasoDocumentada":true,"unidadRegistro":"LITROS"}',
    fuente = 'MINSA/INS Guías Alimentarias para la Población Peruana',
    organismo_autor = 'MINSA/INS',
    version_anio = 'No especificada',
    referencia = 'Referencia trazable: 6–8 vasos/día. Sin equivalencia ml/vaso documentada y adoptada por el sistema, el criterio es NO_CALCULABLE.',
    tipo_fuente = 'REFERENCIA_EXTERNA'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.codigo = 'AGUA_1000ML_DIA';

UPDATE criterios_rubrica_perfil_habitos c
SET nombre = 'Cafeína suplementaria: umbral total EFSA observado desde suplementos',
    parametros_json = '{"maxMg":400,"alcanceUmbral":"TODAS_LAS_FUENTES","fuenteObservada":"SOLO_SUPLEMENTOS"}',
    fuente = 'EFSA caffeine safety',
    organismo_autor = 'European Food Safety Authority',
    version_anio = '2015',
    referencia = 'EFSA: hasta 400 mg/día considera todas las fuentes. >400 mg/día sólo desde suplementos = SUPERA_UMBRAL_TOTAL_DESDE_SUPLEMENTOS; <=400 mg/día sólo desde suplementos = SIN_EXCESO_DOCUMENTADO_SOLO_POR_SUPLEMENTOS, sin afirmar seguridad total.',
    tipo_fuente = 'REFERENCIA_EXTERNA'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.codigo = 'CAFEINA_SUPLEMENTARIA_400MG_DIA';

UPDATE criterios_rubrica_perfil_habitos c
SET fuente = 'OMS Healthy Diet / WHO carbohydrate guideline 2023',
    organismo_autor = 'World Health Organization',
    version_anio = '2023',
    referencia = 'Rango 45–75% de energía preservado como candidato de la rúbrica; requiere validación experta antes de activación.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.codigo = 'CARBOHIDRATOS_45_75_ENERGIA';

UPDATE criterios_rubrica_perfil_habitos c
SET fuente = 'OMS Healthy Diet / WHO total fat guideline 2023',
    organismo_autor = 'World Health Organization',
    version_anio = '2023',
    referencia = 'Rango 15–30% de energía preservado como candidato de la rúbrica; requiere validación experta antes de activación.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.codigo = 'GRASAS_15_30_ENERGIA';

UPDATE criterios_rubrica_perfil_habitos c
SET fuente = 'OMS sodium guideline',
    organismo_autor = 'World Health Organization',
    version_anio = '2012',
    referencia = 'OMS: adultos, menos de 2000 mg/día de sodio. Criterio candidato sujeto a validación experta.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.codigo = 'SODIO_MENOR_2000MG_DIA';

UPDATE criterios_rubrica_perfil_habitos c
SET fuente = 'NIH ODS Exercise and Athletic Performance',
    organismo_autor = 'NIH Office of Dietary Supplements',
    version_anio = 'Actual',
    referencia = 'Registro descriptivo de creatina; no genera penalización automática.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 0.90
  AND c.codigo = 'CREATINA_DESCRIPTIVA';
