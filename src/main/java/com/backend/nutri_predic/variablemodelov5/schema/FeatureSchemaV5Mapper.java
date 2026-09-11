package com.backend.nutri_predic.variablemodelov5.schema;

import com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response;
import java.util.*;
import org.springframework.stereotype.*;

@Component
public class FeatureSchemaV5Mapper {
    public static final String SCHEMA_VERSION = "variables-modelo-v5";
    public static final List<String> FEATURE_NAMES =
            List.of(
                    "edad",
                    "peso_kg",
                    "altura_cm",
                    "tipo_objetivo_fisico",
                    "promedio_kcal_7d",
                    "promedio_proteina_g_7d",
                    "promedio_carbohidratos_g_7d",
                    "promedio_grasas_g_7d",
                    "promedio_fibra_g_7d",
                    "promedio_azucar_g_7d",
                    "promedio_sodio_mg_7d",
                    "promedio_proteina_suplementaria_g_7d",
                    "promedio_creatina_g_7d",
                    "promedio_cafeina_mg_7d",
                    "promedio_carbohidratos_suplementarios_g_7d",
                    "promedio_grasas_suplementarias_g_7d",
                    "promedio_cantidad_comidas_7d",
                    "promedio_consumo_agua_7d",
                    "proporcion_desayuno_7d",
                    "proporcion_snacks_7d",
                    "promedio_comidas_cocinadas_7d");

    public FeatureSchemaV5 mapear(VariablesModeloV5Response r) {
        var f = r.features();
        var m = new LinkedHashMap<String, Object>();
        Object[] v = {
            f.edad(),
            f.pesoKg(),
            f.alturaCm(),
            f.tipoObjetivoFisico(),
            f.promedioKcal7d(),
            f.promedioProteina7d(),
            f.promedioCarbohidratos7d(),
            f.promedioGrasas7d(),
            f.promedioFibra7d(),
            f.promedioAzucar7d(),
            f.promedioSodio7d(),
            f.promedioProteinaSuplementaria7d(),
            f.promedioCreatina7d(),
            f.promedioCafeina7d(),
            f.promedioCarbohidratosSuplementarios7d(),
            f.promedioGrasasSuplementarias7d(),
            f.promedioCantidadComidas7d(),
            f.promedioConsumoAgua7d(),
            f.proporcionDesayuno7d(),
            f.proporcionSnacks7d(),
            f.promedioComidasCocinadas7d()
        };
        for (int i = 0; i < v.length; i++) m.put(FEATURE_NAMES.get(i), v[i]);
        return new FeatureSchemaV5(SCHEMA_VERSION, Collections.unmodifiableMap(m));
    }
}
