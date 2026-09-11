-- La guía MINSA/INS expresa la recomendación en ambas unidades:
-- 2 a 2.5 litros de agua, aproximadamente 6 a 8 vasos al día.
-- El sistema ya persiste RegistroHabito.consumoAgua en litros, por lo que no
-- necesita adoptar una equivalencia artificial de mililitros por vaso.
UPDATE criterios_rubrica_perfil_habitos c
SET parametros_json = '{"referenciaVasos":"6-8 vasos/dia","referenciaLitrosMin":2.0,"referenciaLitrosMax":2.5,"litrosMinimos":2.0,"unidadRegistro":"LITROS","sinPenalizacionSuperior":true}',
    fuente = 'MINSA/INS Guías Alimentarias para la Población Peruana',
    organismo_autor = 'MINSA/INS',
    version_anio = '2019',
    referencia = 'MINSA/INS: 2 a 2.5 litros de agua, aproximadamente 6 a 8 vasos al día. El mínimo de 2 L se evalúa sobre el registro factual en litros; superar 2.5 L no se penaliza como exceso. Conversión no inferida: la misma referencia publica litros y vasos. REGLA_OPERACIONAL_DEL_INSTRUMENTO: adherencia semanal y peso 15/15.'
FROM rubricas_perfil_habitos r
WHERE c.rubrica_id = r.id
  AND r.codigo = 'RUBRICA_PERFIL_HABITOS_V1'
  AND r.version = 1.00
  AND c.codigo = 'AGUA_6_8_VASOS_DIA';
