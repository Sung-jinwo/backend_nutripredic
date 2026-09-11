ALTER TABLE suplementos_cliente
    ADD COLUMN creatina_g_por_toma NUMERIC(12,4) NULL,
    ADD COLUMN cafeina_mg_por_toma NUMERIC(12,4) NULL,
    ADD COLUMN sodio_mg_por_toma NUMERIC(12,4) NULL;

INSERT INTO criterios_consumo (
    codigo, version, estado, ventana_dias, validada, observacion,
    fuente_referencia, validado_por, validado_en, version_evaluador,
    metadata_validacion, regla_global
)
SELECT
    'PCS_SUPLEMENTACION_DIARIA_ADULTOS', 1, 'ACTIVO', 1, TRUE,
    'Tamizaje diario para adultos sanos. No sustituye evaluación clínica; embarazo, lactancia, medicación o sensibilidad individual requieren criterio profesional.',
    'FDA: Spilling the Beans (400 mg/día para la mayoría de adultos); EFSA Journal 2015;13(5):4102',
    'REFERENCIA_EXTERNA_OFICIAL', CURRENT_TIMESTAMP, 'pcs-factual-v1',
    '{"poblacion":"adultos_sanos","limitaciones":["embarazo","lactancia","sensibilidad_individual","medicacion"]}',
    'EXCESO_COMPONENTES_V1'
WHERE NOT EXISTS (
    SELECT 1 FROM criterios_consumo
    WHERE codigo = 'PCS_SUPLEMENTACION_DIARIA_ADULTOS' AND version = 1
);

INSERT INTO reglas_criterio_consumo (
    criterio_id, alcance, componente_tipo, cantidad_referencia,
    ambito_aporte, unidad_referencia, unidad_normalizada, ventana_dias,
    requiere_composicion, tipo_evaluador, fuente_referencia_criterio,
    version_referencia, observacion_metodologica
)
SELECT
    r.id, 'COMPONENTE', 'CAFEINA', 400,
    'SOLO_SUPLEMENTOS', 'MG', 'MG', 1,
    TRUE, 'MAYOR_QUE',
    'https://www.fda.gov/consumers/consumer-updates/spilling-beans-how-much-caffeine-too-much | https://www.efsa.europa.eu/en/topics/topic/caffeine',
    'FDA-2024 / EFSA-2015',
    'El registro captura cafeína de suplementos. Otras fuentes dietarias deben declararse para interpretar el total real.'
FROM criterios_consumo r
WHERE r.codigo = 'PCS_SUPLEMENTACION_DIARIA_ADULTOS' AND r.version = 1
  AND NOT EXISTS (
      SELECT 1 FROM reglas_criterio_consumo c
      WHERE c.criterio_id = r.id AND c.componente_tipo = 'CAFEINA'
  );
