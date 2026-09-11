-- V51: Corrección crítica historial perfil — completar campos faltantes para reconstruir fechaCorte sin fallback silencioso
-- No modifica migraciones históricas. Agrega columnas faltantes auditadas.
ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS realiza_actividad_fisica BOOLEAN;
ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS tipo_actividad_fisica VARCHAR(120);
ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS tipo_entrenamiento VARCHAR(30);
-- Cliente ya tiene tipo_entrenamiento (nuevo campo estructurado), historico lo refleja
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS tipo_entrenamiento VARCHAR(30);
