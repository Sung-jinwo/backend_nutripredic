# Catálogo precargado de alimentos

V24 añade 12 alimentos al catálogo utilizado por `GET /api/alimentos`. Se carga mediante Flyway al iniciar el backend, también en producción; no depende del navegador ni de la disponibilidad de USDA durante el uso.

Fuente: USDA FoodData Central, datos públicos CC0. Valores consultados mediante `POST /fdc/v1/foods` el 18-09-2026. Referencia: **100 g de la preparación descrita**, no una pieza, taza ni porción genérica. Los nombres del catálogo son traducciones de las descripciones originales; crudo y cocido no son intercambiables.

| FDC ID | Descripción original |
| --- | --- |
| 169756 | Rice, white, long-grain, regular, raw, unenriched |
| 171287 | Egg, whole, raw, fresh |
| 171477 | Chicken, broilers or fryers, breast, meat only, cooked, roasted |
| 171688 | Apples, raw, with skin (Includes foods for USDA's Food Distribution Program) |
| 168462 | Spinach, raw |
| 170026 | Potatoes, flesh and skin, raw |
| 169910 | Mangos, raw |
| 170379 | Broccoli, raw |
| 168917 | Quinoa, cooked |
| 171077 | Chicken, broiler or fryers, breast, skinless, boneless, meat only, raw |
| 171705 | Avocados, raw, all commercial varieties |
| 173424 | Egg, whole, cooked, hard-boiled |

Se transcriben los nutrientes USDA 1003 (proteínas), 1005 (carbohidratos) y 1004 (grasas). Un cero es un valor declarado, no una imputación. Las kcal del catálogo siguen el cálculo existente 4/4/9; no se presentan como energía transcrita de USDA. Cada composición conserva su enlace FDC como procedencia.

La migración no cambia tablas ni contratos y no sobrescribe alimentos o composiciones existentes. La vigencia comienza el día de instalación en Lima. Si es necesario retirar este catálogo, hacerlo mediante una nueva migración que desactive únicamente estas referencias; no borrar las filas ni los registros diarios que las referencien, ni editar V24 después de ejecutarla.

Documentación y licencia: https://fdc.nal.usda.gov/api-guide/ . Detalle individual: https://fdc.nal.usda.gov/food-details/{FDC_ID}/nutrients .
