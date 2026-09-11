CREATE TABLE IF NOT EXISTS factores_actividad_regla (
  id BIGSERIAL PRIMARY KEY,
  nivel_actividad VARCHAR(30) NOT NULL,
  factor NUMERIC(6,4) NOT NULL,
  fuente_referencia VARCHAR(400) NOT NULL,
  version_referencia VARCHAR(80) NOT NULL,
  vigente_desde DATE,
  vigente_hasta DATE,
  estado VARCHAR(30) NOT NULL,
  validada BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_factor_nivel_version UNIQUE (nivel_actividad, version_referencia)
);
