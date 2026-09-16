# Precarga administrativa en el backend

`DataSeederOrchestrator.seedBaseData` inicia la precarga al arrancar Spring Boot.
`AdminDemoDataSeeder` contiene cuatro casos artificiales y persiste los usuarios,
perfiles históricos, alimentos con macros, agua, suplemento con composición y
consumo diario. No necesita scripts ni solicitudes de escritura desde React.
Compilar un JAR no conecta ni carga PostgreSQL: lo hace el proceso al iniciarse.

## Activación

La precarga está activada por defecto tanto para el JAR como para Docker. Define
`DEMO_SEED_ENABLED=false` para despliegues que no deban recibir clientes de prueba.
No necesitas ejecutar un script ni configurar una clave para crear las entradas.
El perfil `test` y la configuración de pruebas desactivan el orquestador.

Opcional: proporciona `DEMO_SEED_PASSWORD` con al menos 12 caracteres para poder
iniciar sesión con las cuentas `demo-admin-v1-1@e2e.nutripredic.local` hasta
`demo-admin-v1-4@e2e.nutripredic.local`. Sin esa variable se crean contraseñas
aleatorias privadas, no registradas en logs. No se publican contraseñas en Git.
Los arranques posteriores no cambian las contraseñas ni sobrescriben registros.

## Salidas e indicadores

Los casos 1 y 3 declaran cafeína suplementaria de 450 y 500 mg, exclusivamente
para probar la rúbrica de exceso. NO son dosis recomendadas. Los otros declaran
100 y 0 mg. Las respuestas automatizadas del test tienen 0, 4, 2 y 5 aciertos
para probar los niveles de conocimiento, sin modificar la fórmula PCC.

El backend llama al servicio IA para obtener las metas del día anterior y
ejecuta el ciclo diario oficial, con Random Forest y Gemini configurados.
El proceso externo se ejecuta una sola vez por caso en segundo plano, sin
bloquear el arranque ni hacer reintentos indefinidos. Los tiempos y las
probabilidades proceden de ejecuciones reales, nunca de valores insertados.
PCC, PCS y TPP utilizan sus servicios habituales y las relaciones persistidas.

Si IA, Gemini o la rúbrica no están disponibles, la carga queda PARCIAL: no se
inventan resultados válidos. Revisa los logs y reintenta desde Análisis con el
cliente de prueba, o reinicia el backend una vez corregida la configuración.
El mismo día reutiliza clientes, registros, ciclos y respuestas. Un arranque
en otra fecha añade el registro del día anterior sin eliminar el histórico.

`GET /api/admin/demo-data/status`, protegido para ADMIN, informa el estado y la
cantidad de cuentas de prueba. El frontend solo consulta ese estado y muestra
un aviso de demostración en las vistas administrativas. Al terminar la
precarga, actualiza el módulo para leer los indicadores persistidos.

## Procedencia

Son entradas artificiales `GENERATED_E2E_INPUTS`, identificadas con nombre DEMO,
dominio E2E y marca `NO_USAR_ENTRENAMIENTO`. No son un dataset público ni datos
reales para validar la efectividad del modelo. No se crean etiquetas ground
truth de entrenamiento. Los agregados administrativos incluyen demostración;
no los utilices como evidencia experimental de tesis. Desactivar el seed no
elimina los datos previamente guardados. La base de datos del servidor conserva
la evidencia y el banner sigue visible mientras existan esas cuentas.
