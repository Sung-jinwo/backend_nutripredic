# NutriPredic: Guia de desarrollo del backend

## 1. Stack y versiones

| Componente | Version |
|---|---|
| Java | 21 LTS |
| Spring Boot | 4.1.0 |
| Hibernate | 7.4.x |
| PostgreSQL | produccion |
| H2 | tests (MODE=PostgreSQL) |
| Flyway | activo |
| JWT | jjwt 0.12.6 |
| OpenAPI | springdoc 3.1.0 |
| Bean Validation | via spring-boot-starter-validation |

## Comandos

```bash
./mvnw test              # 12 tests, H2 en memoria
./mvnw package -DskipTests  # genera JAR
```

Tests de integration: `@SpringBootTest` + `@AutoConfigureMockMvc`, base H2 con Flyway.

## 2. Proposito del proyecto

NutriPredic es un sistema web de analisis nutricional con modelo predictivo basado en IA. El backend centraliza la logica de negocio, protege la informacion, administra los datos nutricionales, extrae variables para el modelo y se comunica con el servicio de IA en Python.

La solución estará separada en tres componentes:

```text
NutriPredict/
├── nutripredict-frontend/  # React + TypeScript
├── nutripredict-backend/   # Spring Boot + Java
└── nutripredict-ai/        # Python + Scikit-learn
```

El frontend será responsable de la interfaz, los formularios y la visualización. El backend administrará la seguridad, los usuarios, los clientes, los datos nutricionales, la persistencia, las reglas de negocio, los indicadores y la API REST. El servicio de IA preparará los datos, ejecutará el modelo y devolverá las predicciones.

## 3. Arquitectura y flujo de información

La comunicación entre los componentes se realizará mediante solicitudes HTTP y datos en formato JSON:

```text
React + TypeScript
	│ REST / JSON
	▼
Spring Boot
   ├──────────────► PostgreSQL
   └──────────────► Servicio de IA
			  │
			  ▼
		   Python + Scikit-learn
			  │
			  ▼
		      Predicción
```

Cada módulo tendrá una responsabilidad concreta y no deberá contener lógica perteneciente a otro módulo sin una justificación técnica. El flujo interno será:

```text
Controller → Service → Repository → PostgreSQL
```

### Capas de cada módulo

Los módulos principales seguirán esta estructura:

```text
modulo/
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

- **Controller:** recibe las solicitudes HTTP y devuelve las respuestas. No debe contener lógica de negocio compleja.
- **DTO:** define los datos de entrada y salida de la API. Las entidades JPA no se expondrán directamente al frontend.
- **Entity:** representa las tablas de PostgreSQL mediante JPA/Hibernate.
- **Repository:** proporciona el acceso a los datos mediante Spring Data JPA.
- **Service:** contiene las reglas y operaciones de negocio del módulo.

## 4. Estructura del proyecto

La organización actual y prevista del backend es la siguiente:

```text
src/
├── main/
│   ├── java/com/backend/nutri_predic/
│   │   ├── config/
│   │   ├── security/
│   │   ├── auth/
│   │   ├── usuario/
│   │   ├── cliente/            # + historial_perfiles_cliente (vigente_hasta)
│   │   ├── actividadfisica/    # onboarding + gpaq (METs para V6)
│   │   ├── alimentacion/       # habito/ (encabezado diario) + alimento/lineas + nutricion/ + plantillas
│   │   ├── suplemento/         # catalogo, cliente/habitual, consumo real, composicion
│   │   ├── conocimiento/       # instrumento/ + evaluacion/ (PCC oficial) + practica/ + gemini/
│   │   ├── consumo/            # rubrica + evaluacion PCS oficial
│   │   ├── perfilhabitos/      # ground truth Y (rubrica/evaluaciones, NO es PCS)
│   │   ├── prediccionmodelo/   # modelo/ + evento/ + post/ (única arquitectura de predicción)
│   │   ├── variablemodelov5/ + variablemodelov6/ + datasetmodelov5/ + datasetmodelov6/
│   │   ├── indicador/          # pcc/ + pcs/ + tpp/ oficiales (IndicadorService legacy eliminado)
│   │   ├── dashboard/          # agregador admin (sin lógica de cálculo)
│   │   ├── historial/          # solo historial integrado (lee predicciones_modelo)
│   │   ├── inicio/             # home cliente (agregación read-only + siguiente acción)
│   │   ├── estudio/            # diseño CONTROL/EXPERIMENTAL (trazabilidad tesis)
│   │   ├── plandia/            # planes/cumplimientos (objetivos) + intervenciones post-PCS
│   │   ├── ml/                 # cliente FastAPI (frontera Python)
│   │   └── common/
│   └── resources/
│       ├── application.properties
│       └── db/migration/
└── test/
```

El modulo `indicador` centraliza el calculo de metricas. No duplicar indicadores en `dashboard`.

El modulo `variablemodelo` es el extractor desacoplado de variables para el modelo. No debe depender de `PrediccionService`, Python ni de la definicion de Y.

## 5. Configuración, seguridad y autenticación

### Configuración general

La carpeta `config/` contendrá la configuración transversal del backend:

- `SecurityConfig`: configurará Spring Security, las rutas públicas y protegidas y la autorización por roles.
- `CorsConfig`: permitirá la comunicación entre el frontend React y el backend.
- `OpenApiConfig`: habilitará la documentación y visualización de la API mediante Swagger/OpenAPI.

La configuración de la aplicación y de la conexión con PostgreSQL se mantendrá en `src/main/resources/application.properties`.

### Seguridad con JWT

La carpeta `security/` contendrá:

- `JwtService`: generación y validación de tokens.
- `JwtAuthenticationFilter`: lectura y validación del token en cada solicitud protegida.
- `CustomUserDetailsService`: carga de los datos del usuario autenticado.

El proceso de autenticación será:

1. Recibir las credenciales.
2. Validar el usuario y la contraseña.
3. Generar y devolver un JWT.
4. Validar el token en las solicitudes protegidas.
5. Identificar al usuario autenticado.
6. Verificar los permisos asociados a su rol.

Los roles iniciales serán `CLIENTE` y `ADMIN`.

## 6. Módulos y responsabilidades

### Auth

Gestionará el registro, inicio y cierre de sesión, la autenticación y la generación de JWT. Si se implementa un refresh token, también gestionará su renovación.

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/refresh
```

### Usuario

Gestionará la información general de los usuarios: identificador, email, contraseña, nombre, rol, estado y fechas de creación y actualización. Un usuario con rol `CLIENTE` estará asociado a un registro de cliente.

```text
GET /api/usuarios/me
PUT /api/usuarios/{id}
```

### Cliente

Gestiona la informacion del cliente: edad, sexoBiologico, pesoKg, alturaCm, objetivoFisico/tipoObjetivoFisico, realizaActividadFisica, diasEntrenamientoSemana (1..7 o null), tipoActividadFisica, tipoEntrenamiento, duracionPromedioSesionMinutos, objetivoEnergetico (derivado por backend, nunca manual de cliente), IMC (calculado), estado, datasetGroupId. Los niveles manuales `nivelConocimiento`/`nivelConsumo`/`ultimaEvaluacion` fueron eliminados (V55): PCC oficial vive en `resultados_tests`, PCS oficial en `evaluaciones_consumo`. El campo `imc` se calcula en la entidad como `pesoKg / (alturaCm/100)^2`.

```text
GET /api/clientes
GET /api/clientes/{id}
PUT /api/clientes/{id}
GET /api/clientes/{id}/estado
```

### Hábitos

Gestionará los registros diarios de hábitos alimenticios mediante la entidad `RegistroHabito`. Se almacenarán la fecha, cantidad de comidas, consumo de agua, proteínas, tipo de alimentación, nivel de organización, desayuno, snacks, alimentos, comidas cocinadas, restricciones y consumo de suplementos.

```text
POST /api/habitos
GET /api/habitos/cliente/{clienteId}
GET /api/habitos/cliente/{clienteId}/{fecha}
PUT /api/habitos/{id}
DELETE /api/habitos/{id}
```

Estos datos serán variables de entrada para el análisis predictivo.

### Suplementos

Gestionará el catálogo de suplementos y las asignaciones a los clientes mediante `SuplementoCatalogo` y `SuplementoCliente`. El catálogo incluirá nombre, tipo, descripción, beneficios y recomendaciones. La asignación incluirá cantidad, unidad, frecuencia, tiempo de uso, estado y fechas de inicio y fin.

```text
GET /api/suplementos
POST /api/suplementos
GET /api/clientes/{clienteId}/suplementos
POST /api/clientes/{clienteId}/suplementos
PUT /api/clientes/{clienteId}/suplementos/{suplementoId}
DELETE /api/clientes/{clienteId}/suplementos/{suplementoId}
```

### Conocimiento nutricional

Gestionará las preguntas de evaluación mediante `PreguntaConocimiento`. Cada pregunta tendrá el enunciado, cuatro opciones, respuesta correcta, categoría y dificultad. Las categorías podrán incluir proteínas, carbohidratos, grasas, vitaminas, minerales, alimentación y suplementación.

### Tests

Gestionará la ejecución de las evaluaciones mediante `ResultadoTest` y `RespuestaTest`. El sistema deberá obtener las preguntas, recibir las respuestas, compararlas, calcular el puntaje y porcentaje, determinar el nivel de conocimiento y guardar el resultado asociado al cliente.

Los niveles serán `BAJO`, `MEDIO` y `ALTO`.

```text
POST /api/tests/respuestas
GET /api/clientes/{clienteId}/tests
GET /api/clientes/{clienteId}/tests/{testId}
```

El porcentaje obtenido será una fuente para calcular el indicador de clientes con bajo conocimiento.

### Predicciones (arquitectura única: `prediccionmodelo/`)

Gestiona solicitudes y resultados mediante `PrediccionModelo` + `EventoAnalisis` (+ `ProcedimientoAnalisis`). Clasificación `ADECUADO/MEJORABLE/CRITICO` con probabilidades, `fechaCorte`, `modelVersion`, `schemaVersion`, `inferenceMs`. El flujo legacy (`Prediccion`, `VariablePrediccion`, `ModeloPredictivo`, tablas `predicciones`/`variables_prediccion`/`modelos_predictivos`/`historial_registros`) fue eliminado en Fase 2 (V56).

```text
POST /api/analisis-predictivo              # endpoint productivo oficial V5
GET /api/clientes/{clienteId}/analisis-predictivo/preparacion
GET /api/clientes/{clienteId}/predicciones-modelo
```

Toda predicción pasa por `POST /api/analisis-predictivo`, `EventoAnalisis SOFTWARE_IA`,
`ModeloPredictivoService`/`ModeloPredictivoV6Service` y FeatureSchemaV5/V6.

### Historial

Historial integrado (lee `predicciones_modelo`, tests, hábitos, nutrición). El controlador legacy `/api/historial/cliente/*` fue eliminado en Fase 2.

```text
GET /api/clientes/{clienteId}/historial-integrado
```

### Indicadores

El módulo `indicador` calculará dinámicamente las métricas a partir de la información almacenada en PostgreSQL. No se deberán registrar manualmente valores que puedan obtenerse mediante consultas.

- **Porcentaje de clientes con bajo conocimiento:** `(clientes con conocimiento BAJO / clientes evaluados) × 100`.
- **Porcentaje de clientes con alto consumo:** `(clientes con consumo ALTO / clientes evaluados) × 100`.
- **Tiempo promedio de predicción:** `suma de tiempos de predicción / cantidad de predicciones`.

### Dashboard

Proporcionará al frontend la información del panel administrativo utilizando los servicios oficiales (`Pcc/Pcs/TppIndicatorService`). No deberá concentrar lógica de cálculo. El `modeloActivo` se deriva de la última `predicciones_modelo` (sin tabla de modelos); el operativo manual es `null` por contrato.

```text
GET /api/admin/dashboard
```

Podra entregar total de clientes, total de predicciones, indicadores PCC/PCS/TPP oficiales, modelo activo (última predicción), graficos, tendencias, distribuciones y evolucion de clientes.

### Variables del modelo (extractor desacoplado)

El modulo `variablemodelo` extrae las features X sin depender de la definicion de Y, del modelo ML ni de Python.

- `VariablesModeloService.construir(clienteId, fechaCorte)` retorna un `VariablesModeloResponse` tipado.
- El contrato incluye: perfil, habitos (ventana configurable), suplementos aplicables, conocimiento historico (desde `ResultadoTest`, no desde `Cliente.nivelConocimiento`), calidad de datos y metadata.
- La ventana de habitos se configura con `app.variables-modelo.ventana-habitos-dias` (default 7).
- `fechaCorte` garantiza que ningun dato posterior se filtre.
- No existe imputacion automatica: los valores ausentes se reportan en `camposFaltantes`.
- Los snapshots persistentes (`SnapshotVariablesModelo`) conservan el JSON completo con `schemaVersion`.

```text
GET /api/interno/variables-modelo/clientes/{clienteId}?fechaCorte=YYYY-MM-DD
```

El flujo legacy de `PrediccionService` (llamada HTTP a Python, `Map<String,Object>`, `nivelRiesgo`) permanece separado. No utilizar ese flujo como dataset definitivo.

## 7. Comunicacion con el servicio de IA

Spring Boot no ejecutará directamente el modelo Python. El backend preparará la información del cliente, hábitos, resultados de conocimiento y consumo; posteriormente enviará las variables mediante una solicitud HTTP al servicio de IA.

El servicio Python será responsable de preparar los datos, ejecutar el modelo, calcular la probabilidad y devolver el resultado. Spring Boot será responsable de registrar el tiempo de ejecución, guardar la predicción, conservar las variables utilizadas, asociar el modelo activo y devolver el resultado al frontend.

El flujo completo será:

```text
1. El cliente solicita un análisis.
2. React envía la solicitud al backend.
3. Spring Boot obtiene los datos del cliente.
4. Spring Boot prepara las variables y las envía al servicio de IA.
5. Python ejecuta el modelo y devuelve la predicción.
6. Spring Boot registra el tiempo de respuesta.
7. Spring Boot guarda la predicción, sus variables y el modelo utilizado.
8. React recibe y muestra el resultado.
```

Cada predicción almacenará el tiempo en segundos, por ejemplo `48.350`. Este valor permitirá calcular posteriormente el tiempo promedio de predicción.

La tabla `variables_prediccion` conservará el nombre, valor e importancia de cada variable, por ejemplo conocimiento, consumo, agua y cantidad de comidas. Esto permitirá analizar qué variables influyen más en el modelo.

## 8. Base de datos y migraciones

Se utiliza PostgreSQL. Las tablas base fueron creadas por Hibernate `ddl-auto=update` y las nuevas tablas por Flyway.

Tablas existentes:

```text
usuarios, clientes, historial_perfiles_cliente, registros_habitos,
registros_alimentos, alimentos_catalogo (+composiciones/equivalencias),
suplementos_catalogo, suplementos_cliente, registros_consumo_suplemento,
instrumentos_conocimiento (+preguntas/temas), resultados_tests,
respuestas_test, resultados_tema_test, evaluaciones_gpaq,
evaluaciones_consumo (+detalles/snapshots), predicciones_modelo,
eventos_analisis, procedimientos_analisis, planes_diarios,
cumplimientos_diarios, evaluaciones_perfil_habitos (+rubrica/resultados),
estudios, participaciones_estudio
```

Migraciones en `src/main/resources/db/migration/`: baseline limpio Fase 3
(`V1__core`, `V2__alimentacion`, `V3__suplementacion`, `V4__prediccion`,
`V5__conocimiento`, `V6__indicadores` + `R__seed_catalogos.sql` repeatable).
El historial V1..V56 quedó archivado en `db/migration-legacy/` (fuera de Flyway).
Incluye FKs, UNIQUEs, CHECKs de enums e índices. `ddl-auto=validate`:
Flyway es la autoridad del esquema (tests siguen con H2 `create-drop`, Flyway off).

Reglas:
- Nuevos cambios de esquema via Flyway (nueva migracion versionada).
- Una migracion ejecutada no se modifica.
- `ddl-auto=update` sigue activo en produccion; evaluar cambiar a `validate` antes de deploy real.
- Tests usan `ddl-auto=create-drop` + Flyway sobre H2.

## 9. Orden de desarrollo

El backend se construirá progresivamente:

1. **Configuración:** Spring Boot, PostgreSQL, JPA, Flyway, Spring Security, JWT, CORS y Swagger.
2. **Seguridad:** usuarios, registro, inicio de sesión, JWT, roles y autorizaciones.
3. **Gestión de clientes:** perfiles, estados y relación entre usuarios y clientes.
4. **Datos nutricionales:** hábitos, suplementos, conocimiento y tests.
5. **Historial:** evaluaciones, evolución y cambios del cliente.
6. **Predicción:** modelos, variables, predicciones y comunicación con Python.
7. **Indicadores:** bajo conocimiento, alto consumo y tiempo promedio de predicción.
8. **Dashboard:** KPIs, gráficos, tendencias, distribuciones y resultados.

## 10. Principios de desarrollo

El codigo debera cumplir las siguientes reglas:

1. Mantener la logica de negocio en los servicios, no en los controllers.
2. Utilizar DTOs para las solicitudes y respuestas de la API.
3. Utilizar repositories exclusivamente para el acceso a datos.
4. Validar las entradas mediante Bean Validation.
5. Manejar los errores mediante un `GlobalExceptionHandler` y excepciones de negocio.
6. Proteger los endpoints con Spring Security, JWT y roles.
7. Utilizar Flyway para administrar la evolucion de PostgreSQL.
8. Mantener el codigo Python fuera del proyecto Java y comunicarse con el mediante una API.
9. Registrar cada prediccion, el modelo utilizado, las variables y el tiempo de ejecucion.
10. Conservar el historial de las evaluaciones y predicciones.
11. Calcular los indicadores a partir de los datos almacenados.
12. Evitar la duplicacion de logica y respetar la separacion entre modulos.

### Reglas del extractor (variablemodelo)

- `VariablesModeloService` no debe importar ni depender de `PrediccionService`, `NivelRiesgo`, Python ni `app.ai.url`.
- No definir la variable dependiente (Y) ni reglas de ML sin aprobacion explicita.
- No inventar reglas PCS (clasificacion de suplementos) sin definicion metodologica.
- No utilizar el flujo legacy de `PrediccionService` como dataset definitivo para el modelo.
- Mantener `fechaCorte` como garantia de que ningun dato posterior se filtre.
- No imputar valores ausentes; reportarlos en `camposFaltantes`.

## 11. Resultado esperado

Al finalizar el desarrollo, un cliente podra registrarse e iniciar sesion, completar su informacion personal, registrar habitos y suplementos, realizar una evaluacion de conocimiento, solicitar un analisis predictivo, consultar el resultado y revisar su historial.

El administrador podra gestionar clientes, preguntas y suplementos; consultar evaluaciones, predicciones y modelos; revisar los indicadores y visualizar el dashboard con sus estadisticas.

El resultado sera un backend modular, mantenible y escalable, preparado para integrar el modelo predictivo de Inteligencia Artificial sin mezclar la logica del modelo con la logica principal del sistema.
