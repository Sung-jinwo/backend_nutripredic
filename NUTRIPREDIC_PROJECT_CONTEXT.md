# NutriPredic: Contexto Global del Proyecto

## 1. Contexto de tesis

NutriPredic es un sistema web para analisis nutricional predictivo. El objetivo academico es demostrar que un modelo de machine learning puede predecir el estado nutricional de un cliente a partir de sus habitos, consumo de suplementos y nivel de conocimiento nutricional. La solucion tiene tres componentes: frontend (React), backend (Spring Boot) y servicio de IA (Python/FastAPI).

## 2. Variable independiente

Habitos alimenticios, consumo de suplementos y nivel de conocimiento nutricional del cliente. El extractor `VariablesModeloService` (backend) consolida estas variables en un contrato tipado sin depender del modelo ML ni de la definicion de Y.

## 3. Variable dependiente

**No definida.** La variable dependiente (Y) aun no tiene definicion metodologica aprobada. Candidatos en discusion: adherencia nutricional, riesgo de deficiencia, estado nutricional compuesto. El backend actual NO impone ninguna Y. El flujo legacy de `PrediccionService` usa `nivelRiesgo` como placeholder, pero NO es la Y definitiva.

## 4. Indicadores PCC, PCS y TPP

| Indicador | Estado | Detalle |
|---|---|---|
| **PCC** (Porcentaje Clientes bajo Conocimiento) | Implementado parcial | Calcula `% BAJO / evaluados` desde `Cliente.nivelConocimiento` (campo operativo). Limitacion: no usa historico de `ResultadoTest`. |
| **PCS** (Porcentaje Clientes alto Consumo Suplementos) | Clasificacion configurable por exceso de componentes | El backend reconstruye aportes de alimentos y suplementos a `fechaCorte`, evalua referencias configuradas y aplica `EXCESO_COMPONENTES_V1`. Los limites y su respaldo metodologico pertenecen a la rubrica; no existen umbrales clinicos hardcodeados ni seeds. |
| **TPP** (Tiempo Promedio Prediccion) | Implementado parcial | Solo registra latencia tecnica de la llamada HTTP al servicio IA (`tiempoEjecucion` en `Prediccion`). No existe un TPP metodologico que incluya extraccion + prediccion + post-procesamiento. |

## 5. Arquitectura global vigente

```
React + TypeScript (Vite)
       | REST / JSON
       v
Spring Boot 4.1.0 + Java 21
  |                |
  v                v
PostgreSQL    Servicio IA (Python/FastAPI) - NO IMPLEMENTADO
```

Flujo interno backend: `Controller -> Service -> Repository -> PostgreSQL`.

El extractor `VariablesModeloService` opera como capa independiente: no llama a Python, no crea predicciones, no depende de `PrediccionService`.

## 6. Estado funcional backend

| Modulo | Estado |
|---|---|
| Auth (registro, login, logout, JWT) | Operativo |
| Usuario (perfil, /me) | Operativo |
| Cliente (CRUD, perfil fisico, IMC calculado) | Operativo |
| Habitos (CRUD diario, ventana temporal) | Operativo |
| Suplementos (catalogo + asignaciones con fechas) | Operativo |
| Conocimiento (preguntas, opciones) | Operativo |
| Tests (evaluacion, puntaje, nivel BAJO/MEDIO/ALTO) | Operativo |
| Modelos predictivos (versiones, activacion) | Operativo (sin modelo real) |
| Predicciones (flujo legacy con `Map<String,Object>`) | Operativo (depende de servicio IA externo) |
| Historial | Operativo basico |
| Indicadores (PCC, alto consumo, TPP) | Operativo parcial |
| Dashboard (KPIs admin) | Operativo basico |
| Variables del modelo (extractor desacoplado) | Operativo (Fase 3.3) |
| Snapshots | Operativo |

Build: 12 tests, 0 failures. BUILD SUCCESS.

## 7. Estado funcional frontend

Frontend en React + TypeScript con Vite. Componentes principales:

- Habitos como pagina principal con registro diario
- Modal/sheet para subtareas y formularios
- Suplementos habituales vs consumo diario separados
- Servicios centralizados con contratos del backend
- Responsive
- Build obligatorio antes de considerar fase cerrada

## 8. Modelo de datos relevante

Tablas principales:

```
usuarios, clientes (edad, peso_kg, altura_cm, objetivo_fisico, imc calculado),
registros_habitos, suplementos_catalogo, suplementos_cliente,
preguntas_conocimiento, resultados_tests, respuestas_test,
modelos_predictivos, predicciones, variables_prediccion,
historial_registros, snapshots_variables_modelo
```

`snapshots_variables_modelo`: persiste JSON completo de la extraccion con `schema_version`, `fecha_corte`, `ventana_habitos_dias`. Creada por Flyway V1.

## 9. Contratos importantes entre frontend y backend

- **Extractor**: `GET /api/interno/variables-modelo/clientes/{clienteId}?fechaCorte=YYYY-MM-DD` retorna `VariablesModeloResponse` (perfil, habitos, suplementos, conocimiento, calidadDatos, metadata).
- **Auth**: POST `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`. JWT Bearer.
- **Cliente**: PUT `/api/clientes/{id}` acepta `edad`, `pesoKg`, `alturaCm`, `objetivoFisico`. IMC calculado por backend.
- **Habitos**: CRUD completo con validacion de acceso por cliente.
- **Suplementos**: catalogo + asignaciones con `fechaInicio`/`fechaFin`/`activo`.
- **Tests**: POST `/api/tests/respuestas` envia respuestas, backend calcula puntaje y nivel.
- **Dashboard**: GET `/api/admin/dashboard` (solo ADMIN).

El frontend NO debe enviar `nivelConsumo`, `nivelRiesgo` ni features del modelo como datos de entrada. El extractor los ignora.

## 10. VariablesModeloService y snapshots

`VariablesModeloService.construir(clienteId, fechaCorte)` extrae features X tipadas:

- **Perfil**: edad, pesoKg, alturaCm, imc, objetivoFisico. Excluye: clienteId, usuarioId, nivelConsumo.
- **Habitos**: ventana configurable (default 7 dias), 7 agregaciones + tipoAlimentacion/nivelOrganizacion del ultimo dia. `alimentos`/`restricciones` NO son features.
- **Suplementos**: filtrados por `fechaInicio <= fechaCorte` y `(fechaFin == null || fechaFin >= fechaCorte)`. Sin clasificacion PCS.
- **Conocimiento**: ultimo `ResultadoTest` con fecha <= fechaCorte (NO `Cliente.nivelConocimiento`).
- **Calidad**: camposFaltantes, advertencias, sin imputacion.

Snapshots persistentes en tabla `snapshots_variables_modelo`. Mismo snapshot = misma extraccion, independiente de cambios posteriores al perfil.

Config: `app.variables-modelo.schema-version=variables-modelo-v1`, `ventana-habitos-dias=7`, `zone-id=America/Lima`.

### 10.1. Estado tecnico de PCS

`ConsumoEvaluacionExtractor` reconstruye el consumo factual dentro de la ventana que termina en
`fechaCorte`. Los registros posteriores quedan excluidos. La normalizacion utiliza exclusivamente la
composicion y las equivalencias aplicables ya persistidas; un dato ausente o no convertible permanece
`null` y nunca se transforma en cero.

`PcsClassificationEngine` puede aplicar a cada criterio uno de los operadores tecnicos configurables
(`MAYOR_QUE`, `MAYOR_O_IGUAL`, `MENOR_QUE`, `MENOR_O_IGUAL`, `IGUAL`, `ENTRE`) y conservar una traza
con resultado `CUMPLE`, `NO_CUMPLE` o `NO_CALCULABLE`. Cada criterio declara si compara el aporte
`TOTAL_DIETA` (alimentos + suplementos) o `SOLO_SUPLEMENTOS`. En este contrato `CUMPLE` significa que se
cumple la condicion configurada de exceso, no que el consumo sea saludable.

`ExcesoComponentePcsAggregationRule` implementa la regla productiva `EXCESO_COMPONENTES_V1`: un criterio
`CUMPLE` produce `ALTO`, aunque existan otros no calculables; todos los criterios calculables en
`NO_CUMPLE` producen `NO_ALTO`; y, si no hay exceso pero falta al menos un dato requerido, produce
`NO_DETERMINADA`. Una rubrica sin regla ejecutable conserva la salida:

```text
altoConsumo = null
estadoClasificacion = NO_DETERMINADA
motivo = CRITERIO_NO_IMPLEMENTADO
```

No hay umbrales clinicos ni seeds metodologicos en codigo o migraciones: componente, referencia, unidad,
operador, ventana, alcance y fuente se cargan en la rubrica validada. `IndicadorService.porcentajeNivelConsumoOperativo` conserva el indicador
legacy basado en `Cliente.nivelConsumo`; no constituye ground truth PCS. El dashboard publica
el resultado oficial calculado exclusivamente por `PcsIndicatorService` desde evaluaciones PCS validas y
determinables; la ausencia de estas evaluaciones se representa con `null`, no con cero.

## 11. Estado actual de Machine Learning

- **Servicio Python/FastAPI**: NO implementado.
- **Modelo entrenado**: NO existe.
- **Algoritmo**: no seleccionado.
- **Dataset**: el extractor backend genera las features X. La variable Y no esta definida.
- **Encoding/scaling/imputacion**: responsabilidad del servicio Python, no del backend.
- **Precision/accuracy**: sin datos.
- El `PrediccionService` legacy conserva la llamada histórica a `app.ai.url` y el contrato `nivelRiesgo` + `probabilidad`, pero `POST /api/predicciones` está deprecado y bloqueado con `410 Gone`. No es un pipeline productivo.

## 12. Decisiones metodologicas pendientes

- Definicion de la variable dependiente (Y).
- Criterios de clasificacion ADECUADO/MEJORABLE/CRITICO (si reemplazan BAJO/MEDIO/ALTO).
- Carga administrativa de referencias PCS validadas para las poblaciones y ventanas metodologicamente aprobadas.
- Validacion externa del PCC.
- TPP metodologico (vs latencia tecnica actual).
- Algoritmo de ML y estrategia de entrenamiento.
- Estrategia de encoding, scaling e imputacion.
- Generacion de recomendaciones post-prediccion.

## 13. Limitaciones conocidas

- El perfil fisico (peso, altura, edad) no tiene historial; representa el valor operativo actual. Si cambia entre dos extracciones con la misma fechaCorte, el resultado difiere (snapshot resuelve esto al persistir).
- Las asignaciones de suplementos pueden haber sido sobrescritas o eliminadas; el estado historico no puede garantizarse.
- `alimentos` y `restricciones` son texto libre y no se transforman en features.
- Los cortes BAJO/MEDIO/ALTO son tecnicamente vigentes pero metodologicamente provisionales.
- `ddl-auto=update` sigue activo en produccion; evaluar cambiar a `validate` antes de deploy real.
- No existe imputacion automatica de valores ausentes.
- La clasificacion PCS solo es determinable con una rubrica activa, validada, vigente y con referencias completas; cualquier aporte requerido ausente permanece no calculable.

## 14. Proxima fase

Integracion del servicio de IA (Python/FastAPI):
1. Definir la variable dependiente (Y) con aprobacion metodologica.
2. Implementar el servicio Python con FastAPI.
3. Preparar dataset desde los snapshots de `VariablesModeloService`.
4. Entrenar modelo, evaluar metricas.
5. Conectar backend -> Python reemplazando o complementando el flujo legacy.

## 15. Ultimo checkpoint funcional

Fase 3.3 completada:
- Extractor desacoplado operativo con 4 tests de integration.
- Snapshots persistentes con versionado.
- Contrato tipado sin fuga temporal.
- `PrediccionService` legacy aislado y su endpoint de escritura bloqueado.
- Build: 12/12 tests, BUILD SUCCESS.
- Sin regresiones en auth, perfil, habitos, suplementos, tests ni indicadores.

---

## ULTIMA ACTUALIZACION

- **Fase actual**: 3.3 (extractor de variables desacoplado)
- **Fecha**: 2026-08-24
- **Estado de la fase**: COMPLETADA y verificada
- **Cambios relevantes**:
  1. VariablesModeloService implementado como extractor desacoplado de Y/ML/Python
  2. Contrato tipado VariablesModeloResponse con 8 sub-records
  3. Ventana de habitos configurable con corte temporal estricto
  4. Conocimiento extraido desde ResultadoTest historico (no desde Cliente.nivelConocimiento)
  5. Snapshots persistentes con schema_version en tabla snapshots_variables_modelo
  6. Calidad de datos con camposFaltantes y advertencias, sin imputacion
  7. 4 tests de integration: fuga temporal, anti-sobrescritura, ausencias, snapshots
  8. PrediccionService legacy confirmado como flujo aislado y bloqueado para nuevas ejecuciones
  9. Migraciones Flyway: V1 (snapshots) + R (campos perfil cliente)
  10. Build verde: 12 tests, 0 failures
- **Siguiente paso recomendado**: Definir variable dependiente (Y) e iniciar implementacion del servicio Python/FastAPI.
