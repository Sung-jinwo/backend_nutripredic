-- V56: limpieza estructural Fase 2 (una responsabilidad = una fuente de verdad).
--
-- Predicción legacy (reemplazada por predicciones_modelo + eventos_analisis):
--   predicciones ............ solo lectura histórica (POST bloqueado 410, sin frontend)
--   variables_prediccion .... escrita solo por el flujo legacy muerto
--   modelos_predictivos ..... CRUD /api/modelos sin consumidor (versión vive en
--                             predicciones_modelo.model_version string)
--   historial_registros ..... cero escrituras y cero lecturas en el backend
--
-- Conocimiento duplicado (PCC oficial = instrumentos/resultados_tests;
-- práctica IA = sesiones/preguntas_generadas/respuestas_adaptativas):
--   respuestas_preguntas_generadas_ia ... huérfana desde V27 (sin entidad)
--   preguntas_conocimiento_ia ........... banco paralelo del flujo adaptativo eliminado
--   respuestas_conocimiento_ia .......... duplicaba respuestas_test oficiales
--   progreso_conocimiento_cliente ....... agregado redundante sin lectores
--   criterios_clasificacion_instrumento . entidad sin repository/service/tests
--   instrumentos_temas .................. entidad sin repository/service/tests

DROP TABLE IF EXISTS variables_prediccion CASCADE;
DROP TABLE IF EXISTS predicciones CASCADE;
DROP TABLE IF EXISTS modelos_predictivos CASCADE;
DROP TABLE IF EXISTS historial_registros CASCADE;
DROP TABLE IF EXISTS respuestas_preguntas_generadas_ia CASCADE;
DROP TABLE IF EXISTS respuestas_conocimiento_ia CASCADE;
DROP TABLE IF EXISTS preguntas_conocimiento_ia CASCADE;
DROP TABLE IF EXISTS progreso_conocimiento_cliente CASCADE;
DROP TABLE IF EXISTS criterios_clasificacion_instrumento CASCADE;
DROP TABLE IF EXISTS instrumentos_temas CASCADE;
