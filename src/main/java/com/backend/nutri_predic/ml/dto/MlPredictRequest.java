package com.backend.nutri_predic.ml.dto;

import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.variablemodelo.schema.FeatureSchemaV4;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record MlPredictRequest(String schemaVersion, Map<String, Object> features) {
    public static MlPredictRequest from(FeatureSchemaV4 schema) {
        MlFeatureVectorV4.from(schema);
        return new MlPredictRequest(schema.schemaVersion(), copia(schema.features()));
    }

    public static MlPredictRequest from(FeatureSchemaV5 schema) {
        if (schema == null
                || !FeatureSchemaV5Mapper.SCHEMA_VERSION.equals(schema.schemaVersion())
                || !schema.features()
                        .keySet()
                        .equals(java.util.Set.copyOf(FeatureSchemaV5Mapper.FEATURE_NAMES))) {
            throw new BusinessException("El vector ML requiere exactamente las features v5");
        }
        return new MlPredictRequest(schema.schemaVersion(), copia(schema.features()));
    }

    public static MlPredictRequest from(FeatureSchemaV6 schema) {
        if (schema == null
                || !FeatureSchemaV6Mapper.SCHEMA_VERSION.equals(schema.schemaVersion())
                || !schema.features()
                        .keySet()
                        .equals(java.util.Set.copyOf(FeatureSchemaV6Mapper.FEATURE_NAMES))) {
            throw new BusinessException("El vector ML requiere exactamente las features v6");
        }
        return new MlPredictRequest(schema.schemaVersion(), copia(schema.features()));
    }

    private static Map<String, Object> copia(Map<String, Object> valores) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(valores));
    }
}
