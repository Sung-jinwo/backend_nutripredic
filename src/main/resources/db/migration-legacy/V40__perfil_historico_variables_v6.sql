ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS dias_entrenamiento_semana INTEGER;
ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS duracion_promedio_sesion_minutos INTEGER;
ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS objetivo_energetico VARCHAR(20);
