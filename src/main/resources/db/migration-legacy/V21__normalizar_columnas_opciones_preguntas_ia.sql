DO $migration$
DECLARE
    columna_duplicada TEXT;
    columna_definitiva TEXT;
    hay_conflictos BOOLEAN;
    hay_nulos BOOLEAN;
BEGIN
    FOR columna_duplicada, columna_definitiva IN
        SELECT *
        FROM (VALUES
            ('opciona', 'opcion_a'),
            ('opcionb', 'opcion_b'),
            ('opcionc', 'opcion_c'),
            ('opciond', 'opcion_d')
        ) AS columnas(duplicada, definitiva)
    LOOP
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = current_schema()
              AND table_name = 'preguntas_generadas_ia'
              AND column_name = columna_duplicada
        ) THEN
            IF NOT EXISTS (
                SELECT 1 FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = 'preguntas_generadas_ia'
                  AND column_name = columna_definitiva
            ) THEN
                EXECUTE format(
                    'ALTER TABLE preguntas_generadas_ia RENAME COLUMN %I TO %I',
                    columna_duplicada, columna_definitiva
                );
            ELSE
                EXECUTE format(
                    'SELECT EXISTS (SELECT 1 FROM preguntas_generadas_ia WHERE %1$I IS NOT NULL AND %2$I IS NOT NULL AND %1$I <> %2$I)',
                    columna_duplicada, columna_definitiva
                ) INTO hay_conflictos;

                IF hay_conflictos THEN
                    RAISE EXCEPTION
                        'No se puede consolidar %.%: existen valores divergentes',
                        columna_duplicada, columna_definitiva;
                END IF;

                EXECUTE format(
                    'UPDATE preguntas_generadas_ia SET %1$I = %2$I WHERE %1$I IS NULL AND %2$I IS NOT NULL',
                    columna_definitiva, columna_duplicada
                );

                EXECUTE format(
                    'ALTER TABLE preguntas_generadas_ia DROP COLUMN %I',
                    columna_duplicada
                );
            END IF;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = current_schema()
              AND table_name = 'preguntas_generadas_ia'
              AND column_name = columna_definitiva
        ) THEN
            RAISE EXCEPTION 'Falta la columna definitiva %', columna_definitiva;
        END IF;

        EXECUTE format(
            'SELECT EXISTS (SELECT 1 FROM preguntas_generadas_ia WHERE %I IS NULL)',
            columna_definitiva
        ) INTO hay_nulos;

        IF hay_nulos THEN
            RAISE EXCEPTION 'La columna definitiva % contiene valores nulos', columna_definitiva;
        END IF;

        EXECUTE format(
            'ALTER TABLE preguntas_generadas_ia ALTER COLUMN %I SET NOT NULL',
            columna_definitiva
        );
    END LOOP;
END
$migration$;
