package com.backend.nutri_predic.variablemodelov6.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FeatureSchemaV6Mapper {
    public static final String SCHEMA_VERSION = "variables-modelo-v6";
    public static final List<String> FEATURE_NAMES = List.of(
            "edad", "peso_kg", "altura_cm", "sexo_biologico", "tipo_objetivo_fisico",
            "objetivo_energetico", "dias_entrenamiento_semana",
            "duracion_promedio_sesion_minutos", "consumo_kcal_dia_anterior",
            "consumo_proteina_g_dia_anterior", "consumo_carbohidratos_g_dia_anterior",
            "consumo_grasas_g_dia_anterior", "consumo_agua_ml_dia_anterior");

    public FeatureSchemaV6 mapear(Map<String, Object> valores) {
        var resultado = new LinkedHashMap<String, Object>();
        FEATURE_NAMES.forEach(nombre -> resultado.put(nombre, valores.get(nombre)));
        return new FeatureSchemaV6(SCHEMA_VERSION, Collections.unmodifiableMap(resultado));
    }
}
