package com.backend.nutri_predic.ml.client;

import com.backend.nutri_predic.ml.dto.MlPredictResponse;
import com.backend.nutri_predic.ml.dto.MlProbabilidadesResponse;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.variablemodelo.schema.FeatureSchemaV4Mapper;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import java.math.BigDecimal;

public final class MlPredictResponseValidator {
    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final BigDecimal UNO = BigDecimal.ONE;
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.001");

    private MlPredictResponseValidator() {}

    public static void validar(MlPredictResponse response) {
        validar(response, FeatureSchemaV4Mapper.SCHEMA_VERSION);
    }

    public static void validar(MlPredictResponse response, String schemaEsperado) {
        if (response == null
                || response.probabilidades() == null
                || vacio(response.schemaVersion())
                || vacio(response.modelVersion())
                || vacio(response.clasificacion()))
            throw new ModeloMlException("Respuesta del modelo incompleta");
        if (!FeatureSchemaV4Mapper.SCHEMA_VERSION.equals(schemaEsperado)
                && !FeatureSchemaV5Mapper.SCHEMA_VERSION.equals(schemaEsperado)
                && !FeatureSchemaV6Mapper.SCHEMA_VERSION.equals(schemaEsperado)) {
            throw new ModeloMlException("SchemaVersion solicitado no soportado");
        }
        if (!schemaEsperado.equals(response.schemaVersion()))
            throw new ModeloMlException("SchemaVersion incompatible devuelto por el modelo");
        try {
            ClasificacionPerfilHabitos.valueOf(response.clasificacion());
        } catch (IllegalArgumentException error) {
            throw new ModeloMlException("Clasificación inválida devuelta por el modelo", error);
        }
        MlProbabilidadesResponse p = response.probabilidades();
        validarProbabilidad(p.adecuado());
        validarProbabilidad(p.mejorable());
        validarProbabilidad(p.critico());
        if (p.adecuado()
                        .add(p.mejorable())
                        .add(p.critico())
                        .subtract(UNO)
                        .abs()
                        .compareTo(TOLERANCIA)
                > 0) throw new ModeloMlException("Las probabilidades del modelo no suman 1");
        if ((FeatureSchemaV5Mapper.SCHEMA_VERSION.equals(schemaEsperado)
                        || FeatureSchemaV6Mapper.SCHEMA_VERSION.equals(schemaEsperado))
                && (response.inferenceMs() == null
                        || response.inferenceMs().compareTo(CERO) < 0
                        || response.inferredAt() == null)) {
            throw new ModeloMlException("Respuesta sin metadata de inferencia válida");
        }
    }

    private static void validarProbabilidad(BigDecimal valor) {
        if (valor == null || valor.compareTo(CERO) < 0 || valor.compareTo(UNO) > 0)
            throw new ModeloMlException("Probabilidad inválida devuelta por el modelo");
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
