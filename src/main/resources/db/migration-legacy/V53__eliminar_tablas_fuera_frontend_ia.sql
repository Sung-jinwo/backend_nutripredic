-- V53: Limpieza tablas fuera de módulos Frontend + Modelo predictivo V6
-- Frontend usa: usuarios, clientes, historial_perfiles_cliente, unidades_medida,
--  registros_habitos, alimentos_catalogo, registros_alimentos, plantillas_comida(+detalles),
--  composiciones_nutricionales_alimentos, equivalencias_unidades_alimentos,
--  suplementos_catalogo, suplementos_cliente, registros_consumo_suplemento,
--  composiciones_suplementos, componentes_composicion_suplementos, equivalencias_unidades_suplementos,
--  historial_suplementos_cliente, suplementos_ingredientes (frontend /suplementos/{id}/ingredientes),
--  evaluaciones_actividad_fisica, evaluaciones_gpaq, mapeos_categoria_actividad,
--  reglas_requerimiento_nutricional, factores_actividad_regla,
--  eventos_analisis, predicciones_modelo, procedimientos_analisis, fallos_ciclo_post_prediccion,
--  predicciones, variables_prediccion (lectura histórica),
--  planes_diarios, cumplimientos_diarios, intervenciones_consumo_diarias,
--  preguntas_conocimiento, instrumentos_conocimiento(+preguntas/temas), criterios_clasificacion_instrumento,
--  resultados_tests, respuestas_test, resultados_tema_test, temas_conocimiento,
--  sesiones_conocimiento_ia, preguntas_generadas_ia, respuestas_preguntas_generadas_ia, trazas_llamadas_gemini,
--  respuestas_adaptativas_ia, preguntas_conocimiento_ia, respuestas_conocimiento_ia, progreso_conocimiento_cliente,
--  rubricas_perfil_habitos(+dimensiones/criterios), evaluaciones_perfil_habitos(+resultados),
--  criterios_consumo, reglas_criterio_consumo, evaluaciones_consumo, snapshots_evaluacion_consumo, detalles_evaluacion_consumo,
--  estudios, participaciones_estudio
-- AI usa exactamente 28 X V6 (schemas.py FeaturesV6): edad,peso_kg,altura_cm,tipo_objetivo_fisico,
--  promedio_*_7d (kcal/proteina/carbo/grasas/fibra/azucar/sodio), promedio suplementaria, creatina, cafeina, etc,
--  cantidad_comidas, agua, desayuno, snacks, comidas_cocinadas, objetivo_energetico, dias_entrenamiento, duracion, 4 METs
--
-- Tablas eliminadas: sin consumo frontend ni features, sin FK crítica
-- No tocar: historial_registros (usado por /api/historial/cliente legacy, mantener hasta migración frontend),
--  modelos_predictivos (legacy pero sin uso frontend, se deja para auditoría, borrar en V54 si se confirma)

DROP TABLE IF EXISTS estados_observacion_diaria CASCADE;
DROP TABLE IF EXISTS ingredientes_catalogo CASCADE;
DROP TABLE IF EXISTS resultados_componentes_consumo CASCADE;
-- snapshots_variables_modelo y contenidos_educativos se conservan como HISTORICO V4 hasta retirar código variablemodelo (evita fallo Hibernate)
