ALTER TABLE composiciones_suplementos
    ADD COLUMN IF NOT EXISTS energia_kcal_porcion NUMERIC(10,2);
