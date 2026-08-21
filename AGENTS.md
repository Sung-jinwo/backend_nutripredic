# NutriPredic: Guía de desarrollo del backend

## 1. Propósito del proyecto

NutriPredic será un sistema web de análisis nutricional que utilizará un modelo predictivo basado en Inteligencia Artificial. El backend será el componente encargado de centralizar la lógica de negocio, proteger la información, administrar los datos nutricionales y comunicarse con el servicio de Inteligencia Artificial desarrollado en Python.

El backend se desarrollará con:

- Java y Spring Boot
- Spring Web para la API REST
- Spring Data JPA y Hibernate para la persistencia
- Spring Security y JWT para la autenticación y autorización
- PostgreSQL como base de datos
- Flyway para las migraciones
- Bean Validation para validar los datos recibidos
- Swagger/OpenAPI para documentar la API

La solución estará separada en tres componentes:

```text
NutriPredict/
├── nutripredict-frontend/  # React + TypeScript
├── nutripredict-backend/   # Spring Boot + Java
└── nutripredict-ai/        # Python + Scikit-learn
```

El frontend será responsable de la interfaz, los formularios y la visualización. El backend administrará la seguridad, los usuarios, los clientes, los datos nutricionales, la persistencia, las reglas de negocio, los indicadores y la API REST. El servicio de IA preparará los datos, ejecutará el modelo y devolverá las predicciones.

## 2. Arquitectura y flujo de información

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

## 3. Estructura del proyecto

La organización actual y prevista del backend es la siguiente:

```text
src/
├── main/
│   ├── java/com/backend/nutri_predic/
│   │   ├── config/
│   │   ├── security/
│   │   ├── auth/
│   │   ├── usuario/
│   │   ├── cliente/
│   │   ├── habito/
│   │   ├── suplemento/
│   │   ├── conocimiento/
│   │   ├── test/
│   │   ├── modelo/
│   │   ├── prediccion/
│   │   ├── historial/
│   │   ├── indicador/
│   │   ├── dashboard/
│   │   └── common/
│   └── resources/
│       ├── application.properties
│       └── db/migration/
└── test/
```

El módulo `indicador` se incorporará para centralizar el cálculo de las métricas del sistema. Mientras tanto, los indicadores no deberán duplicarse dentro del módulo `dashboard`.

## 4. Configuración, seguridad y autenticación

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

## 5. Módulos y responsabilidades

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

Gestionará la información específica del cliente: edad, estado, última evaluación, nivel de conocimiento, nivel de consumo, resultado y fechas de creación y actualización. El administrador podrá gestionar todos los clientes y cada cliente podrá consultar su propia información.

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

### Modelos predictivos

Gestionará las versiones del modelo mediante `ModeloPredictivo`. Se almacenarán nombre, versión, algoritmo, descripción, precisión, recall, F1-score, estado y fecha de entrenamiento. Cada predicción deberá identificar la versión del modelo que la generó.

### Predicciones

Gestionará las solicitudes y resultados mediante `Prediccion` y `VariablePrediccion`. Una predicción se relacionará con un cliente, un modelo, las variables utilizadas y el resultado. Deberá registrar fecha, nivel de riesgo, probabilidad, resultado, tiempo de ejecución y observaciones.

```text
POST /api/predicciones
GET /api/predicciones/{id}
GET /api/clientes/{clienteId}/predicciones
```

### Historial

Permitirá consultar la evolución del cliente, incluyendo evaluaciones, nivel de conocimiento, nivel de consumo, resultados, cambios entre evaluaciones y predicciones anteriores.

```text
GET /api/historial/cliente/{clienteId}
GET /api/historial/cliente/{clienteId}/{fecha}
```

### Indicadores

El módulo `indicador` calculará dinámicamente las métricas a partir de la información almacenada en PostgreSQL. No se deberán registrar manualmente valores que puedan obtenerse mediante consultas.

- **Porcentaje de clientes con bajo conocimiento:** `(clientes con conocimiento BAJO / clientes evaluados) × 100`.
- **Porcentaje de clientes con alto consumo:** `(clientes con consumo ALTO / clientes evaluados) × 100`.
- **Tiempo promedio de predicción:** `suma de tiempos de predicción / cantidad de predicciones`.

### Dashboard

Proporcionará al frontend la información del panel administrativo utilizando los servicios correspondientes, especialmente `IndicadorService`. No deberá concentrar toda la lógica de cálculo.

```text
GET /api/admin/dashboard
```

Podrá entregar total de clientes, clientes evaluados, clientes con bajo conocimiento, clientes con alto consumo, tiempo promedio de predicción, total de predicciones, modelo activo, gráficos, tendencias, distribuciones y evolución de clientes.

## 6. Comunicación con el servicio de IA

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

## 7. Base de datos y migraciones

Se utilizará PostgreSQL con claves foráneas para representar las relaciones entre las tablas principales:

```text
usuarios
clientes
registros_habitos
suplementos_catalogo
suplementos_cliente
preguntas_conocimiento
resultados_tests
respuestas_test
modelos_predictivos
predicciones
variables_prediccion
historial_registros
indicadores_sistema
```

Las migraciones se almacenarán en `src/main/resources/db/migration/` y se ejecutarán mediante Flyway en orden, por ejemplo:

```text
V1__crear_usuarios.sql
V2__crear_clientes.sql
V3__crear_habitos.sql
V4__crear_suplementos.sql
V5__crear_conocimiento.sql
V6__crear_tests.sql
V7__crear_modelos_predictivos.sql
V8__crear_predicciones.sql
V9__crear_variables_prediccion.sql
V10__crear_historial.sql
V11__crear_indicadores.sql
```

Una migración ya ejecutada en un ambiente compartido no deberá modificarse. Cualquier cambio posterior se realizará mediante una nueva migración.

## 8. Orden de desarrollo

El backend se construirá progresivamente:

1. **Configuración:** Spring Boot, PostgreSQL, JPA, Flyway, Spring Security, JWT, CORS y Swagger.
2. **Seguridad:** usuarios, registro, inicio de sesión, JWT, roles y autorizaciones.
3. **Gestión de clientes:** perfiles, estados y relación entre usuarios y clientes.
4. **Datos nutricionales:** hábitos, suplementos, conocimiento y tests.
5. **Historial:** evaluaciones, evolución y cambios del cliente.
6. **Predicción:** modelos, variables, predicciones y comunicación con Python.
7. **Indicadores:** bajo conocimiento, alto consumo y tiempo promedio de predicción.
8. **Dashboard:** KPIs, gráficos, tendencias, distribuciones y resultados.

## 9. Principios de desarrollo

El código deberá cumplir las siguientes reglas:

1. Mantener la lógica de negocio en los servicios, no en los controllers.
2. Utilizar DTOs para las solicitudes y respuestas de la API.
3. Utilizar repositories exclusivamente para el acceso a datos.
4. Validar las entradas mediante Bean Validation.
5. Manejar los errores mediante un `GlobalExceptionHandler` y excepciones de negocio.
6. Proteger los endpoints con Spring Security, JWT y roles.
7. Utilizar Flyway para administrar la evolución de PostgreSQL.
8. Mantener el código Python fuera del proyecto Java y comunicarse con él mediante una API.
9. Registrar cada predicción, el modelo utilizado, las variables y el tiempo de ejecución.
10. Conservar el historial de las evaluaciones y predicciones.
11. Calcular los indicadores a partir de los datos almacenados.
12. Evitar la duplicación de lógica y respetar la separación entre módulos.

## 10. Resultado esperado

Al finalizar el desarrollo, un cliente podrá registrarse e iniciar sesión, completar su información personal, registrar hábitos y suplementos, realizar una evaluación de conocimiento, solicitar un análisis predictivo, consultar el resultado y revisar su historial.

El administrador podrá gestionar clientes, preguntas y suplementos; consultar evaluaciones, predicciones y modelos; revisar los indicadores y visualizar el dashboard con sus estadísticas.

El resultado será un backend modular, mantenible y escalable, preparado para integrar el modelo predictivo de Inteligencia Artificial sin mezclar la lógica del modelo con la lógica principal del sistema.
