# backend_nutripredic

## Flujo predictivo oficial

El único endpoint productivo oficial para solicitar una predicción nueva es:

```http
POST /api/analisis-predictivo
```

Su recorrido es:

```text
FeatureSchemaV5
→ EventoAnalisis SOFTWARE_IA
→ ModeloPredictivoService
→ Random Forest V5 o reutilización V5 válida
→ PrediccionModelo
→ cierre del EventoAnalisis
→ medición TPP
```

`POST /api/predicciones` es un contrato histórico deprecado y responde
`410 Gone`. Sus entidades, consultas de historial y componentes V4 se conservan
para reproducibilidad, pero no deben utilizarse para ejecutar predicciones nuevas.

`POST /api/analisis` se conserva como entrada compatible para procedimientos de
análisis. Cuando el procedimiento es `SOFTWARE_IA`, utiliza la misma
instrumentación de `EventoAnalisisService`; no contiene lógica predictiva paralela.

El contrato HTTP congelado para el frontend está documentado en
[`docs/CONTRATO_BACKEND_FRONTEND.md`](docs/CONTRATO_BACKEND_FRONTEND.md).
