-- V54: Limpieza final tablas sin uso Frontend ni IA V6 (complemento V53)
-- Frontend/AI keep: ver V53 comentario + V6 28X
-- Drop histórico V4 y contenido educativo no consumido por UI
DROP TABLE IF EXISTS snapshots_variables_modelo CASCADE;
DROP TABLE IF EXISTS contenidos_educativos CASCADE;
-- predicciones legacy se mantiene lectura 410 hasta confirmar que frontend no usa GET /api/predicciones/{id}
-- modelos_predictivos se mantiene por DashboardService.findFirstByActivoTrue hasta refactorizar dashboard a PrediccionModelo
-- historial_registros se mantiene por /api/historial/cliente legacy, migrar a /historial-integrado antes de borrar
