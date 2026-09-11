ALTER TABLE sesiones_conocimiento_ia
    ADD COLUMN reintento_de_sesion_id BIGINT;

ALTER TABLE sesiones_conocimiento_ia
    ADD CONSTRAINT fk_sesiones_conocimiento_ia_reintento
    FOREIGN KEY (reintento_de_sesion_id) REFERENCES sesiones_conocimiento_ia(id);
