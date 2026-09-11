CREATE TABLE reglas_requerimiento_nutricional (
    id BIGSERIAL PRIMARY KEY, activa BOOLEAN NOT NULL DEFAULT TRUE,
    fuente VARCHAR(160) NOT NULL, version_fuente VARCHAR(60) NOT NULL,
    edad_minima INTEGER NOT NULL, edad_maxima INTEGER NOT NULL,
    realiza_actividad_fisica BOOLEAN NOT NULL, dias_entrenamiento_minimo INTEGER, dias_entrenamiento_maximo INTEGER,
    tipo_actividad_fisica VARCHAR(120), duracion_sesion_minima INTEGER, duracion_sesion_maxima INTEGER,
    objetivo_energetico VARCHAR(20) NOT NULL,
    kcal_objetivo NUMERIC(10,2), proteina_objetivo_g NUMERIC(10,2),
    carbohidratos_objetivo_g NUMERIC(10,2), grasas_objetivo_g NUMERIC(10,2)
);
