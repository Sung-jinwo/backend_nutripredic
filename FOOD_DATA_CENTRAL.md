# Composición pública de alimentos

Fuente: USDA FoodData Central, https://fdc.nal.usda.gov/api-guide/ . Datos de dominio público, CC0 1.0. Cada selección muestra enlace al alimento y conserva su FDC ID en el nombre del registro diario guardado.

Configura `FDC_API_KEY` en el entorno **del backend**, nunca en Vite ni en Git. Solicita una clave gratuita en https://fdc.nal.usda.gov/api-key-signup/ . Sin clave propia se usa DEMO_KEY, limitada a 30 consultas/hora y 50/día por IP: no es una configuración suficiente para producción.

El cliente busca en inglés, selecciona preparación y declara gramos consumidos. Se consultan Foundation, SR Legacy y Survey (FNDDS). No se equipara una receta casera con un alimento genérico. No hay conversión automática de tazas, cucharadas o porciones a gramos sin un peso verificado.

El backend vuelve a consultar el detalle seleccionado al guardar y escala proteínas (1003), grasas (1004) y carbohidratos (1005), todos en gramos por 100 g. Rechaza valores ausentes: cero es válido únicamente si la fuente lo informa. Las kcal continúan calculándose con la fórmula existente 4/4/9, no se pide un nuevo campo al cliente. Se persisten las cantidades calculadas, de modo que futuras consultas de USDA no cambien registros históricos.

Las búsquedas requieren JWT. Fallos, cuotas o claves inválidas permiten continuar con el formulario manual y una notificación; no se generan composiciones inventadas. No se modifica el clasificador ni las reglas PCS.

En suplementos se declara el total consumido de cada registro y se envía `numeroTomas=1`. Al editar registros antiguos, el frontend convierte cantidad × tomas a total para preservar el aporte existente. La composición se configura una vez en Mis suplementos; si faltan componentes o la unidad no es convertible, no debe interpretarse como consumo seguro.
