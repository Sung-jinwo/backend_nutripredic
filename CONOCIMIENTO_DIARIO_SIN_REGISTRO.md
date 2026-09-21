# Día anterior sin registro

La falta de consumo anterior no se imputa como cero ni se reemplaza silenciosamente por otro día. La predicción V6, PCS y orientación personalizada de consumo permanecen pendientes; las metas iniciales siguen independientes.

`POST /api/clientes/{id}/conocimiento/diario-perfil/asegurar` asegura cinco preguntas Gemini para hoy (Lima), usando perfil, metas y errores de evaluaciones respondidas anteriores. Reutiliza la sesión de hoy, incluso respondida. La generación se serializa mediante el bloqueo del cliente; un fallo Gemini puede reintentarse sin sustituir respuestas.

Las sesiones educativas utilizan `pcc-perfil-diario-v1`, carecen de predicción y clasificación y no son válidas para PCC oficial. Su generación no crea eventos de análisis ni tiempos TPP. PCS no se calcula sin consumo. No se altera el clasificador ni las fórmulas nutricionales.

V25 amplía la restricción de origen y distingue la unicidad diaria educativa de la predictiva. Si más tarde se completa el consumo anterior y se ejecuta el ciclo real, su test oficial puede coexistir con el educativo, sin convertir preguntas de perfil en preguntas basadas en consumo ni borrar sus respuestas. Las consultas de fecha priorizan la sesión predictiva existente.

Desplegar backend y migración antes del frontend. Los endpoints anteriores se conservan. Una reversión de código puede mantener V25; no retirar los nuevos permisos del esquema mientras haya sesiones educativas, ni recomponer el índice global eliminando sesiones. Una contracción posterior requiere primero revisar las fechas con ambas fuentes y conservar su historial.
