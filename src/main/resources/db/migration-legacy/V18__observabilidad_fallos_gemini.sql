ALTER TABLE sesiones_conocimiento_ia ADD COLUMN codigo_error_tecnico VARCHAR(80);
ALTER TABLE sesiones_conocimiento_ia ADD COLUMN http_status_gemini INTEGER;
ALTER TABLE sesiones_conocimiento_ia ADD COLUMN fallido_en TIMESTAMPTZ;
