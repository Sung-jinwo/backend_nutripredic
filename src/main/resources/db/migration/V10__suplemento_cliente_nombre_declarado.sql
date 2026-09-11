ALTER TABLE suplementos_cliente
  ADD COLUMN IF NOT EXISTS nombre_declarado VARCHAR(255);

UPDATE suplementos_cliente sc
SET nombre_declarado = s.nombre
FROM suplementos_catalogo s
WHERE sc.suplemento_id = s.id
  AND sc.nombre_declarado IS NULL;

ALTER TABLE suplementos_cliente
  ALTER COLUMN nombre_declarado SET NOT NULL;
