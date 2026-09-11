-- V5 Bloque 5: expansión compatible del objetivo físico estructurado.
-- El texto legacy se conserva y no se infieren categorías para datos existentes.
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS tipo_objetivo_fisico VARCHAR(40);
ALTER TABLE historial_perfiles_cliente ADD COLUMN IF NOT EXISTS tipo_objetivo_fisico VARCHAR(40);
