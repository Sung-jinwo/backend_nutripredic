CREATE TABLE equivalencias_unidades_alimentos (
 id BIGSERIAL PRIMARY KEY, alimento_catalogo_id BIGINT NOT NULL REFERENCES alimentos_catalogo(id), version INTEGER NOT NULL,
 cantidad_origen NUMERIC(12,4) NOT NULL, unidad_origen_id BIGINT NOT NULL REFERENCES unidades_medida(id),
 cantidad_destino NUMERIC(12,4) NOT NULL, unidad_destino_id BIGINT NOT NULL REFERENCES unidades_medida(id),
 fuente_datos VARCHAR(1000), fecha_desde DATE NOT NULL, fecha_hasta DATE, activo BOOLEAN NOT NULL DEFAULT TRUE,
 creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uq_equivalencia_alimento_version UNIQUE(alimento_catalogo_id,version),
 CONSTRAINT ck_equivalencia_cantidades CHECK(cantidad_origen>0 AND cantidad_destino>0),
 CONSTRAINT ck_equivalencia_fechas CHECK(fecha_hasta IS NULL OR fecha_hasta>=fecha_desde)
);
CREATE INDEX ix_equivalencia_alimento_vigencia ON equivalencias_unidades_alimentos(alimento_catalogo_id,activo,fecha_desde,fecha_hasta,version);
ALTER TABLE registros_alimentos ADD COLUMN equivalencia_unidad_id BIGINT NULL REFERENCES equivalencias_unidades_alimentos(id);
CREATE INDEX ix_registro_alimento_equivalencia ON registros_alimentos(equivalencia_unidad_id);
