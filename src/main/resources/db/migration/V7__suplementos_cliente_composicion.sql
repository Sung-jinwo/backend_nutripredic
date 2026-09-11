ALTER TABLE suplementos_cliente
    ADD COLUMN IF NOT EXISTS componentes_declarados VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS energia_kcal_por_toma NUMERIC(12,4),
    ADD COLUMN IF NOT EXISTS proteina_g_por_toma NUMERIC(12,4),
    ADD COLUMN IF NOT EXISTS carbohidratos_g_por_toma NUMERIC(12,4),
    ADD COLUMN IF NOT EXISTS grasas_g_por_toma NUMERIC(12,4);
