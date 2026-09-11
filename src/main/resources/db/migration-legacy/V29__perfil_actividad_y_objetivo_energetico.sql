ALTER TABLE clientes ADD COLUMN IF NOT EXISTS realiza_actividad_fisica BOOLEAN;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS dias_entrenamiento_semana INTEGER;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS tipo_actividad_fisica VARCHAR(120);
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS duracion_promedio_sesion_minutos INTEGER;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS objetivo_energetico VARCHAR(20);
