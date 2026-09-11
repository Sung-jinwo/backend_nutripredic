CREATE TABLE composiciones_nutricionales_alimentos (
    id BIGSERIAL PRIMARY KEY,
    alimento_catalogo_id BIGINT NOT NULL REFERENCES alimentos_catalogo(id),
    version INTEGER NOT NULL,
    cantidad_referencia NUMERIC(12,4) NOT NULL,
    unidad_referencia_id BIGINT NOT NULL REFERENCES unidades_medida(id),
    kcal NUMERIC(12,4),
    proteina_g NUMERIC(12,4),
    carbohidratos_g NUMERIC(12,4),
    grasas_g NUMERIC(12,4),
    fibra_g NUMERIC(12,4),
    azucar_g NUMERIC(12,4),
    sodio_mg NUMERIC(12,4),
    fuente_datos VARCHAR(1000),
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_composicion_alimento_version UNIQUE (alimento_catalogo_id, version),
    CONSTRAINT ck_composicion_referencia_positiva CHECK (cantidad_referencia > 0),
    CONSTRAINT ck_composicion_fechas CHECK (fecha_hasta IS NULL OR fecha_hasta >= fecha_desde),
    CONSTRAINT ck_composicion_kcal_no_negativa CHECK (kcal IS NULL OR kcal >= 0),
    CONSTRAINT ck_composicion_proteina_no_negativa CHECK (proteina_g IS NULL OR proteina_g >= 0),
    CONSTRAINT ck_composicion_carbohidratos_no_negativa CHECK (carbohidratos_g IS NULL OR carbohidratos_g >= 0),
    CONSTRAINT ck_composicion_grasas_no_negativa CHECK (grasas_g IS NULL OR grasas_g >= 0),
    CONSTRAINT ck_composicion_fibra_no_negativa CHECK (fibra_g IS NULL OR fibra_g >= 0),
    CONSTRAINT ck_composicion_azucar_no_negativa CHECK (azucar_g IS NULL OR azucar_g >= 0),
    CONSTRAINT ck_composicion_sodio_no_negativa CHECK (sodio_mg IS NULL OR sodio_mg >= 0)
);

CREATE INDEX ix_composicion_alimento_vigencia
    ON composiciones_nutricionales_alimentos (alimento_catalogo_id, activo, fecha_desde, fecha_hasta, version);

ALTER TABLE registros_alimentos
    ADD COLUMN composicion_nutricional_id BIGINT NULL
    REFERENCES composiciones_nutricionales_alimentos(id);

CREATE INDEX ix_registro_alimento_composicion
    ON registros_alimentos (composicion_nutricional_id);
