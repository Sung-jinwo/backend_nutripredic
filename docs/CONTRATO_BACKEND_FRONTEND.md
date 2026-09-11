# Contrato backend → frontend

Este documento describe los DTO HTTP reales. `null` significa dato no disponible o no
determinable y nunca debe convertirse en cero.

## 1. Análisis predictivo oficial

`POST /api/analisis-predictivo` — usuario autenticado con acceso al cliente.

Request:

```json
{
  "clienteId": 42,
  "fechaCorte": "2026-08-30",
  "participacionEstudioId": null,
  "momento": "BASAL"
}
```

- `clienteId`: entero positivo, obligatorio.
- `fechaCorte`: fecha ISO, obligatoria y no futura.
- `participacionEstudioId`: entero positivo opcional. No incorpora grupo CONTROL.
- `momento`: `BASAL`, `FINAL` o `NO_DETERMINADO`, obligatorio.

Respuesta `201 Created`:

```json
{
  "prediccionId": 81,
  "eventoAnalisisId": 91,
  "fechaCorte": "2026-08-30",
  "momento": "BASAL",
  "clasificacion": "ADECUADO",
  "probabilidades": {
    "ADECUADO": 0.8,
    "MEJORABLE": 0.15,
    "CRITICO": 0.05
  },
  "modelVersion": "rf-v5",
  "schemaVersion": "variables-modelo-v5",
  "inferenceMs": 12.4,
  "inferredAt": "2026-08-30T15:00:01Z",
  "origenResultado": "GENERADO",
  "estado": "VALIDA",
  "analisisIniciadoEn": "2026-08-30T15:00:00Z",
  "resultadoDisponibleEn": "2026-08-30T15:00:02Z"
}
```

`origenResultado` es `GENERADO` o `REUTILIZADO`. Una reutilización también responde
`201` porque la solicitud crea una nueva ejecución instrumentada y un nuevo
`EventoAnalisis`, aunque reutilice una predicción V5 persistida. `inferenceMs` es sólo
metadata técnica del modelo; no es TPP.

Errores usan siempre `ApiErrorResponse`:

```json
{
  "timestamp": "2026-08-30T15:00:02Z",
  "status": 502,
  "error": "Bad Gateway",
  "message": "No se pudo comunicar con el servicio ML",
  "path": "/api/analisis-predictivo",
  "fieldErrors": []
}
```

| HTTP | Significado |
|---|---|
| 400 | JSON/validación inválida, participación inválida o datos técnicamente insuficientes |
| 401 | Falta autenticación válida |
| 403 | El usuario no puede acceder al cliente |
| 404 | Cliente o procedimiento `SOFTWARE_IA` activo inexistente |
| 502 | Servicio/modelo no disponible, respuesta incompatible o error predictivo |
| 500 | Error interno no clasificado |

Ante un fallo predictivo, el `EventoAnalisis` queda `INVALIDA` y
`resultadoDisponibleEn` queda `null`; no se devuelve una respuesta exitosa artificial.

## 2. Indicadores oficiales

Los tres endpoints requieren rol `ADMIN`.

### PCC

`GET /api/admin/indicadores/pcc`

```json
{
  "porcentajePcc": null,
  "totalEvaluadosValidos": 0,
  "totalBajoConocimiento": 0,
  "estadoDisponibilidad": "NO_DISPONIBLE",
  "motivoNoDisponible": "SIN_RESULTADOS_VALIDOS"
}
```

Estados: `DISPONIBLE`, `NO_DISPONIBLE`. El motivo actualmente definido es
`SIN_RESULTADOS_VALIDOS`. La fuente exclusiva es `ResultadoTest` válido; la
clasificación predictiva no se transforma en `BAJO/MEDIO/ALTO`.

Flujo PCC-IA complementario:

1. `POST /api/analisis-predictivo` genera o reutiliza la predicción V5.
2. `POST /api/clientes/{clienteId}/conocimiento/post-modelo/generar` recibe
   `temasPermitidos`, `dificultadPermitida` y `cantidadPreguntas`.
3. `GET /api/clientes/{clienteId}/conocimiento/post-modelo` entrega la sesión, el
   instrumento base y las preguntas adaptativas.
4. Los DTO públicos de preguntas en estado `GENERADA` no contienen
   `respuestaCorrecta` ni `explicacion`.
5. El frontend entrega el conjunto adaptativo completo mediante el endpoint de
   respuestas descrito a continuación.
6. `POST /api/tests/respuestas` continúa siendo el único flujo que corrige el
   instrumento validado y persiste `ResultadoTest`, que alimenta PCC.

#### Generar u obtener la sesión

`POST /api/clientes/{clienteId}/conocimiento/post-modelo/generar` — rol `ADMIN`.

```json
{
  "temasPermitidos": ["PROTEINA"],
  "dificultadPermitida": "MEDIA",
  "cantidadPreguntas": 2
}
```

`GET /api/clientes/{clienteId}/conocimiento/post-modelo` — usuario autenticado con
acceso al cliente.

Mientras la sesión está `GENERADA`, `preguntasAdaptativas` contiene únicamente
enunciado y opciones, y `resultadoAdaptativo` es `null`. No se exponen soluciones.

#### Responder la sesión adaptativa

`POST /api/clientes/{clienteId}/conocimiento/post-modelo/{sesionId}/respuestas`
— usuario autenticado con acceso al cliente.

```json
{
  "respuestas": [
    {
      "preguntaId": 123,
      "opcionSeleccionada": "B"
    },
    {
      "preguntaId": 124,
      "opcionSeleccionada": "A"
    }
  ]
}
```

No se admiten campos adicionales como `respuestaCorrecta`, `explicacion`,
`correcta`, `puntaje` o un nivel calculado por el frontend. La entrega debe contener
exactamente una respuesta para cada pregunta pública de la sesión.

Respuesta `200 OK`:

```json
{
  "sesionId": 51,
  "totalPreguntas": 2,
  "totalRespondidas": 2,
  "correctas": 1,
  "porcentajeAdaptativo": 50.0,
  "estado": "RESPONDIDA",
  "respondidaEn": "2026-08-31T15:00:00Z",
  "respuestas": [
    {
      "preguntaId": 123,
      "opcionSeleccionada": "B",
      "correcta": true,
      "respuestaCorrecta": "B",
      "explicacion": "Feedback educativo"
    }
  ]
}
```

El porcentaje es exclusivamente adaptativo y educativo. No representa PCC y no
produce `BAJO`, `MEDIO` ni `ALTO`.

Política de entrega:

- transición única `GENERADA → RESPONDIDA`;
- una segunda entrega responde `400 Bad Request` y no sobrescribe datos;
- una sesión `IA_NO_DISPONIBLE` no admite respuestas;
- preguntas ajenas, repetidas o un conjunto incompleto responden `400`;
- sesión inexistente o perteneciente a otro cliente responde `404`;
- `GET /api/clientes/{clienteId}/conocimiento/post-modelo` incluye
  `resultadoAdaptativo` con feedback sólo cuando el estado es `RESPONDIDA`.

Las respuestas se almacenan en `respuestas_adaptativas_ia` y se relacionan con la
sesión y la pregunta generada. No tienen relación con `ResultadoTest`.

### PCS

`GET /api/admin/indicadores/pcs`

```json
{
  "porcentajePcs": null,
  "totalEvaluadosValidos": 0,
  "totalAltoConsumo": 0,
  "estadoDisponibilidad": "NO_DISPONIBLE",
  "motivoNoDisponible": "CLASIFICACION_METODOLOGICA_NO_DISPONIBLE"
}
```

Motivos existentes: `CLASIFICACION_METODOLOGICA_NO_DISPONIBLE` y
`POLITICA_SELECCION_EVALUACION_NO_DEFINIDA`. La fuente es `EvaluacionConsumo`; el
campo operativo `Cliente.nivelConsumo` no es PCS.

### TPP

`GET /api/admin/indicadores/tpp`

```json
{
  "promedioTppMs": null,
  "totalAnalisisValidos": 0,
  "totalAnalisisExcluidos": 0,
  "estadoDisponibilidad": "NO_DISPONIBLE",
  "motivoNoDisponible": "SIN_ANALISIS_VALIDOS",
  "exclusiones": {}
}
```

TPP usa exclusivamente eventos `SOFTWARE_IA` válidos y calcula
`resultadoDisponibleEn - analisisIniciadoEn`. Las exclusiones posibles son
`PROCEDIMIENTO_NO_APLICABLE`, `INICIO_AUSENTE`, `RESULTADO_AUSENTE`,
`ORDEN_TEMPORAL_INVALIDO` y `EVENTO_INVALIDO`. `inferenceMs` no participa.

## 3. Dashboard

`GET /api/admin/dashboard` — rol `ADMIN`.

```json
{
  "totalClientes": 10,
  "totalPrediccionesV5": 8,
  "pcc": {
    "porcentajePcc": 50.0,
    "totalEvaluadosValidos": 2,
    "totalBajoConocimiento": 1,
    "estadoDisponibilidad": "DISPONIBLE",
    "motivoNoDisponible": null
  },
  "pcs": {
    "porcentajePcs": null,
    "totalEvaluadosValidos": 0,
    "totalAltoConsumo": 0,
    "estadoDisponibilidad": "NO_DISPONIBLE",
    "motivoNoDisponible": "CLASIFICACION_METODOLOGICA_NO_DISPONIBLE"
  },
  "tpp": {
    "promedioTppMs": null,
    "totalAnalisisValidos": 0,
    "totalAnalisisExcluidos": 0,
    "estadoDisponibilidad": "NO_DISPONIBLE",
    "motivoNoDisponible": "SIN_ANALISIS_VALIDOS",
    "exclusiones": {}
  },
  "porcentajeNivelConsumoOperativo": null,
  "modeloActivo": null
}
```

`pcc`, `pcs` y `tpp` son exactamente los mismos DTO producidos por sus servicios
oficiales. `porcentajeNivelConsumoOperativo` se conserva separado y no debe mostrarse
como PCS. `modeloActivo`, cuando existe, contiene `id`, `nombre` y `version`.

## 4. Contratos legacy excluidos

El frontend nuevo no debe consumir:

- `POST /api/predicciones`: deprecado y bloqueado con `410 Gone`.
- `IndicadorService.indicators()`: cálculo histórico/operativo deprecado.
- `FeatureSchemaV4`: conservado sólo para reproducibilidad histórica.

El único endpoint para iniciar una predicción productiva nueva es
`POST /api/analisis-predictivo`.
