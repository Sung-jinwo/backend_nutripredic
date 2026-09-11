ALTER TABLE rubricas_perfil_habitos
    ADD COLUMN IF NOT EXISTS observacion_validacion VARCHAR(2000);

ALTER TABLE rubricas_perfil_habitos
    ADD COLUMN IF NOT EXISTS vigente_hasta TIMESTAMPTZ;

CREATE TABLE dimensiones_rubrica_perfil_habitos (
    id BIGSERIAL PRIMARY KEY,
    rubrica_id BIGINT NOT NULL REFERENCES rubricas_perfil_habitos(id),
    codigo VARCHAR(60) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(2000),
    puntaje_minimo NUMERIC(6,2),
    puntaje_maximo NUMERIC(6,2),
    peso_porcentual NUMERIC(6,2),
    orden INTEGER NOT NULL,
    CONSTRAINT uq_dimension_perfil_rubrica_codigo UNIQUE (rubrica_id, codigo),
    CONSTRAINT uq_dimension_perfil_rubrica_orden UNIQUE (rubrica_id, orden),
    CONSTRAINT ck_dimension_perfil_puntaje_minimo
        CHECK (puntaje_minimo IS NULL OR puntaje_minimo BETWEEN 0 AND 100),
    CONSTRAINT ck_dimension_perfil_puntaje_maximo
        CHECK (puntaje_maximo IS NULL OR puntaje_maximo BETWEEN 0 AND 100),
    CONSTRAINT ck_dimension_perfil_rango
        CHECK (puntaje_minimo IS NULL OR puntaje_maximo IS NULL OR puntaje_minimo <= puntaje_maximo),
    CONSTRAINT ck_dimension_perfil_peso
        CHECK (peso_porcentual IS NULL OR peso_porcentual BETWEEN 0 AND 100),
    CONSTRAINT ck_dimension_perfil_orden CHECK (orden > 0)
);

CREATE INDEX ix_dimension_perfil_rubrica
    ON dimensiones_rubrica_perfil_habitos (rubrica_id, orden);
