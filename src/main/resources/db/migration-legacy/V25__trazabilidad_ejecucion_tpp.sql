ALTER TABLE eventos_analisis
    ADD COLUMN origen_resultado VARCHAR(30);

ALTER TABLE eventos_analisis
    ADD COLUMN version_evento BIGINT NOT NULL DEFAULT 0;

ALTER TABLE eventos_analisis
    ADD CONSTRAINT ck_evento_analisis_origen_resultado
    CHECK (origen_resultado IS NULL OR origen_resultado IN ('GENERADO', 'REUTILIZADO'));
