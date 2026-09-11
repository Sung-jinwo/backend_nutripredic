-- V43 podía dejar la versión 1.00 en BORRADOR cuando la fila ya existía,
-- porque el INSERT usaba ON CONFLICT DO NOTHING. Completa la publicación
-- técnico-documental definida por V43 sin modificar criterios ni resultados.

UPDATE rubricas_perfil_habitos
SET estado = 'INACTIVA'
WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND version <> 1.00
  AND estado = 'ACTIVA';

UPDATE rubricas_perfil_habitos
SET nombre = 'Rubrica ground truth de perfil de habitos V6',
    descripcion = 'Version 1.00 con validacion tecnico-documental para ground truth real V6. Fuentes: OMS, MINSA/INS, EFSA, NIH-ODS. Ponderaciones D1-D5, tabla de adherencia semanal, cobertura minima 70% y cortes 80/50 son REGLA_OPERACIONAL_DEL_INSTRUMENTO.',
    estado = 'ACTIVA',
    validado_por = NULL,
    validado_en = COALESCE(validado_en, NOW()),
    vigente_desde = COALESCE(vigente_desde, NOW()),
    observacion_validacion = 'Validacion tecnico-documental. No hay comite ni acta de expertos. La validez se deriva de las fuentes externas citadas en fuente_validacion. Version fuente: RUBRICA_PERFIL_HABITOS_V1-1.00. Ponderaciones 30/20/15/15/20, adherencia semanal, cobertura 70%, cortes 80/50 = REGLA_OPERACIONAL_DEL_INSTRUMENTO.',
    tipo_validacion = 'VALIDACION_TECNICO_DOCUMENTAL',
    fuente_validacion = 'OMS Healthy Diet 2025; WHO carbohydrate/fat guidelines 2023; WHO sodium guidance; MINSA/INS Guías Alimentarias para la Población Peruana; EFSA Protein DRV; EFSA Caffeine Safety; NIH-ODS Exercise and Athletic Performance',
    version_fuente = 'RUBRICA_PERFIL_HABITOS_V1-1.00'
WHERE codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND version = 1.00;
