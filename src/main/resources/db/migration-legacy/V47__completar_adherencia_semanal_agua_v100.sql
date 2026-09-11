-- La V1.00 declaró la tabla semanal como REGLA_OPERACIONAL_DEL_INSTRUMENTO,
-- pero dejó el JSON del criterio de agua en NULL. Se conserva la misma curva
-- 0/1/2/3/3/4/5/5 usada por los demás criterios, escalada de 5 a 15 puntos.
UPDATE criterios_rubrica_perfil_habitos c
SET adherencia7d_json = '{"0":0,"1":3,"2":6,"3":9,"4":9,"5":12,"6":15,"7":15}',
    referencia = referencia || ' Tabla operacional semanal aplicada: 0/1/2/3/4/5/6/7 días = 0/3/6/9/9/12/15/15 puntos.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 1.00
  AND c.codigo = 'AGUA_6_8_VASOS_DIA'
  AND c.adherencia7d_json IS NULL;
