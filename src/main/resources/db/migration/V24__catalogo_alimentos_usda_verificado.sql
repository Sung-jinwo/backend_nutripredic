-- Additive, idempotent USDA CC0 reference catalog; no existing composition is replaced.
-- Verified via FoodData Central /foods on 2026-09-18; amounts per 100 grams.
-- Energy below follows the application's existing 4/4/9 macro calculation.
INSERT INTO unidades_medida (codigo, nombre, activa)
SELECT 'G', 'Gramo', TRUE WHERE NOT EXISTS (SELECT 1 FROM unidades_medida WHERE codigo = 'G');

INSERT INTO alimentos_catalogo (nombre, categoria, unidad_base_id, activo)
SELECT datos.nombre, datos.categoria, unidad.id, TRUE
FROM (VALUES
    ('Arroz blanco de grano largo, crudo (USDA)', 'CEREALES', 169756, 7.13, 79.95, 0.66),
    ('Huevo entero, crudo (USDA)', 'HUEVOS', 171287, 12.56, 0.72, 9.51),
    ('Pechuga de pollo sin piel, asada (USDA)', 'CARNES', 171477, 31.02, 0, 3.57),
    ('Manzana con piel, cruda (USDA)', 'FRUTAS', 171688, 0.26, 13.81, 0.17),
    ('Espinaca cruda (USDA)', 'VERDURAS', 168462, 2.86, 3.63, 0.39),
    ('Papa con piel, cruda (USDA)', 'TUBERCULOS', 170026, 2.05, 17.49, 0.09),
    ('Mango crudo (USDA)', 'FRUTAS', 169910, 0.82, 14.98, 0.38),
    ('Brócoli crudo (USDA)', 'VERDURAS', 170379, 2.82, 6.64, 0.37),
    ('Quinua cocida (USDA)', 'CEREALES', 168917, 4.4, 21.3, 1.92),
    ('Pechuga de pollo sin piel, cruda (USDA)', 'CARNES', 171077, 22.5, 0, 2.62),
    ('Palta cruda (USDA)', 'FRUTAS', 171705, 2, 8.53, 14.66),
    ('Huevo entero, sancochado (USDA)', 'HUEVOS', 173424, 12.58, 1.12, 10.61)
) AS datos(nombre, categoria, fdc_id, proteina, carbos, grasas)
JOIN unidades_medida unidad ON unidad.codigo = 'G'
WHERE NOT EXISTS (SELECT 1 FROM alimentos_catalogo existente WHERE existente.nombre = datos.nombre);

INSERT INTO composiciones_nutricionales_alimentos
(alimento_catalogo_id, version, cantidad_referencia, unidad_referencia_id,
 kcal, proteina_g, carbohidratos_g, grasas_g, fuente_datos, fecha_desde, activo, creado_en)
SELECT alimento.id, 1, 100, unidad.id,
       4 * datos.proteina + 4 * datos.carbos + 9 * datos.grasas,
       datos.proteina, datos.carbos, datos.grasas,
       CONCAT('USDA FoodData Central CC0; https://fdc.nal.usda.gov/food-details/', datos.fdc_id,
              '/nutrients; consultado 2026-09-18; kcal derivadas 4/4/9'),
       CAST(CURRENT_TIMESTAMP AT TIME ZONE 'America/Lima' AS DATE), TRUE, CURRENT_TIMESTAMP
FROM (VALUES
    ('Arroz blanco de grano largo, crudo (USDA)', 'CEREALES', 169756, 7.13, 79.95, 0.66),
    ('Huevo entero, crudo (USDA)', 'HUEVOS', 171287, 12.56, 0.72, 9.51),
    ('Pechuga de pollo sin piel, asada (USDA)', 'CARNES', 171477, 31.02, 0, 3.57),
    ('Manzana con piel, cruda (USDA)', 'FRUTAS', 171688, 0.26, 13.81, 0.17),
    ('Espinaca cruda (USDA)', 'VERDURAS', 168462, 2.86, 3.63, 0.39),
    ('Papa con piel, cruda (USDA)', 'TUBERCULOS', 170026, 2.05, 17.49, 0.09),
    ('Mango crudo (USDA)', 'FRUTAS', 169910, 0.82, 14.98, 0.38),
    ('Brócoli crudo (USDA)', 'VERDURAS', 170379, 2.82, 6.64, 0.37),
    ('Quinua cocida (USDA)', 'CEREALES', 168917, 4.4, 21.3, 1.92),
    ('Pechuga de pollo sin piel, cruda (USDA)', 'CARNES', 171077, 22.5, 0, 2.62),
    ('Palta cruda (USDA)', 'FRUTAS', 171705, 2, 8.53, 14.66),
    ('Huevo entero, sancochado (USDA)', 'HUEVOS', 173424, 12.58, 1.12, 10.61)
) AS datos(nombre, categoria, fdc_id, proteina, carbos, grasas)
JOIN alimentos_catalogo alimento ON alimento.nombre = datos.nombre
JOIN unidades_medida unidad ON unidad.codigo = 'G'
WHERE NOT EXISTS (
    SELECT 1 FROM composiciones_nutricionales_alimentos existente WHERE existente.alimento_catalogo_id = alimento.id
);
