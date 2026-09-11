package com.backend.nutri_predic.variablemodelo.schema;

import java.util.*;

public class FeatureSchemaV4Mapper {
    public static final String SCHEMA_VERSION = "variables-modelo-v4";
    public static final List<String> FEATURE_NAMES = List.of(
            "edad", "peso_kg", "altura_cm", "imc",
            "promedio_cantidad_comidas", "promedio_consumo_agua",
            "proporcion_desayuno", "proporcion_snacks",
            "promedio_comidas_cocinadas", "proporcion_consume_suplementos",
            "suplementos_cantidad_aplicables", "suplementos_cantidad_activos_declarados"
    );

    public FeatureSchemaV4 mapear(Map<String, Object> valores) {
        var m = new LinkedHashMap<String, Object>();
        FEATURE_NAMES.forEach(n -> m.put(n, valores.get(n)));
        return new FeatureSchemaV4(SCHEMA_VERSION, Collections.unmodifiableMap(m));
    }
}
