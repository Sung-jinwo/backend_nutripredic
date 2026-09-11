ALTER TABLE clientes ADD COLUMN IF NOT EXISTS dataset_group_id UUID;

UPDATE clientes
SET dataset_group_id = md5(random()::text || clock_timestamp()::text || id::text)::uuid
WHERE dataset_group_id IS NULL;

ALTER TABLE clientes ALTER COLUMN dataset_group_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_clientes_dataset_group_id ON clientes (dataset_group_id);
