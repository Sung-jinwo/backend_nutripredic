package com.backend.nutri_predic.ml.dto;

import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.variablemodelo.schema.FeatureSchemaV4;
import com.backend.nutri_predic.variablemodelo.schema.FeatureSchemaV4Mapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record MlFeatureVectorV4(
        Integer edad,
        @JsonProperty("peso_kg") BigDecimal pesoKg,
        @JsonProperty("altura_cm") BigDecimal alturaCm,
        BigDecimal imc,
        @JsonProperty("promedio_cantidad_comidas") BigDecimal promedioCantidadComidas,
        @JsonProperty("promedio_consumo_agua") BigDecimal promedioConsumoAgua,
        @JsonProperty("proporcion_desayuno") BigDecimal proporcionDesayuno,
        @JsonProperty("proporcion_snacks") BigDecimal proporcionSnacks,
        @JsonProperty("promedio_comidas_cocinadas") BigDecimal promedioComidasCocinadas,
        @JsonProperty("proporcion_consume_suplementos") BigDecimal proporcionConsumeSuplementos,
        @JsonProperty("suplementos_cantidad_aplicables") Integer suplementosCantidadAplicables,
        @JsonProperty("suplementos_cantidad_activos_declarados")
                Integer suplementosCantidadActivosDeclarados) {
    public static final List<String> FEATURE_NAMES = FeatureSchemaV4Mapper.FEATURE_NAMES;

    public static MlFeatureVectorV4 from(FeatureSchemaV4 schema) {
        if (schema == null
                || !FeatureSchemaV4Mapper.SCHEMA_VERSION.equals(schema.schemaVersion())
                || !schema.features().keySet().equals(java.util.Set.copyOf(FEATURE_NAMES)))
            throw new BusinessException("El vector ML requiere exactamente las features v4");
        var f = schema.features();
        return new MlFeatureVectorV4(
                (Integer) f.get("edad"),
                (BigDecimal) f.get("peso_kg"),
                (BigDecimal) f.get("altura_cm"),
                (BigDecimal) f.get("imc"),
                (BigDecimal) f.get("promedio_cantidad_comidas"),
                (BigDecimal) f.get("promedio_consumo_agua"),
                (BigDecimal) f.get("proporcion_desayuno"),
                (BigDecimal) f.get("proporcion_snacks"),
                (BigDecimal) f.get("promedio_comidas_cocinadas"),
                (BigDecimal) f.get("proporcion_consume_suplementos"),
                (Integer) f.get("suplementos_cantidad_aplicables"),
                (Integer) f.get("suplementos_cantidad_activos_declarados"));
    }
}
