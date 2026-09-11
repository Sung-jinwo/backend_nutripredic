package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backend.nutri_predic.ml.client.MlPredictResponseValidator;
import com.backend.nutri_predic.ml.dto.*;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.variablemodelo.schema.FeatureSchemaV4;
import com.backend.nutri_predic.variablemodelo.schema.FeatureSchemaV4Mapper;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class MlPredictContractTests {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void requestDerivaExactamenteLasDoceFeaturesV4SinMetadataYConservaNulos() throws Exception {
        var request = MlPredictRequest.from(schema());
        String cuerpo = json.writeValueAsString(request);

        assertThat(request.schemaVersion()).isEqualTo(FeatureSchemaV4Mapper.SCHEMA_VERSION);
        assertThat(MlFeatureVectorV4.FEATURE_NAMES)
                .containsExactlyElementsOf(FeatureSchemaV4Mapper.FEATURE_NAMES);
        assertThat(request.features().get("imc")).isNull();
        assertThat(cuerpo)
                .contains(
                        "\"schemaVersion\":\"variables-modelo-v4\"",
                        "\"peso_kg\"",
                        "\"suplementos_cantidad_activos_declarados\"");
        assertThat(cuerpo)
                .doesNotContain(
                        "clienteId",
                        "cliente_group_id",
                        "nombre",
                        "email",
                        "grupo",
                        "momento",
                        "conocimiento",
                        "clasificacionReal",
                        "PCC",
                        "PCS",
                        "TPP",
                        "prediccion");
    }

    @Test
    void requestV5ConservaExactamenteLasVeintiunaFeaturesSinIndicadores() throws Exception {
        var features = new LinkedHashMap<String, Object>();
        FeatureSchemaV5Mapper.FEATURE_NAMES.forEach(nombre -> features.put(nombre, null));
        features.put("edad", 29);
        features.put("tipo_objetivo_fisico", "GANAR_MASA");
        var request =
                MlPredictRequest.from(
                        new FeatureSchemaV5(FeatureSchemaV5Mapper.SCHEMA_VERSION, features));

        String cuerpo = json.writeValueAsString(request);

        assertThat(request.schemaVersion()).isEqualTo("variables-modelo-v5");
        assertThat(request.features().keySet())
                .containsExactlyElementsOf(FeatureSchemaV5Mapper.FEATURE_NAMES);
        assertThat(cuerpo)
                .doesNotContain(
                        "inferenceMs",
                        "inferredAt",
                        "clienteId",
                        "clasificacionReal",
                        "PCC",
                        "PCS",
                        "TPP");
    }

    @Test
    void responseV5ExigeYConservaMetadataTecnica() {
        MlPredictResponse valida =
                new MlPredictResponse(
                        "MEJORABLE",
                        new MlProbabilidadesResponse(
                                new BigDecimal("0.2"),
                                new BigDecimal("0.65"),
                                new BigDecimal("0.15")),
                        "modelo-v5",
                        FeatureSchemaV5Mapper.SCHEMA_VERSION,
                        new BigDecimal("4.25"),
                        Instant.parse("2026-08-30T12:00:00Z"));

        MlPredictResponseValidator.validar(valida, FeatureSchemaV5Mapper.SCHEMA_VERSION);
        assertThat(valida.inferenceMs()).isEqualByComparingTo("4.25");
        assertThat(valida.inferredAt()).isEqualTo(Instant.parse("2026-08-30T12:00:00Z"));
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        new MlPredictResponse(
                                                "MEJORABLE",
                                                valida.probabilidades(),
                                                "modelo-v5",
                                                FeatureSchemaV5Mapper.SCHEMA_VERSION),
                                        FeatureSchemaV5Mapper.SCHEMA_VERSION))
                .isInstanceOf(ModeloMlException.class);
    }

    @Test
    void responseAceptaLasTresClasificacionesYRechazaContratosInvalidos() {
        for (String clasificacion : java.util.List.of("ADECUADO", "MEJORABLE", "CRITICO"))
            MlPredictResponseValidator.validar(
                    respuesta(
                            clasificacion,
                            "0.20",
                            "0.65",
                            "0.15",
                            "modelo-v1",
                            FeatureSchemaV4Mapper.SCHEMA_VERSION));
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        respuesta(
                                                "ADECUADO",
                                                "-0.01",
                                                "0.65",
                                                "0.36",
                                                "modelo-v1",
                                                FeatureSchemaV4Mapper.SCHEMA_VERSION)))
                .isInstanceOf(ModeloMlException.class);
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        respuesta(
                                                "ADECUADO",
                                                "1.01",
                                                "0",
                                                "0",
                                                "modelo-v1",
                                                FeatureSchemaV4Mapper.SCHEMA_VERSION)))
                .isInstanceOf(ModeloMlException.class);
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        respuesta(
                                                "ADECUADO",
                                                "0.2",
                                                "0.3",
                                                "0.2",
                                                "modelo-v1",
                                                FeatureSchemaV4Mapper.SCHEMA_VERSION)))
                .isInstanceOf(ModeloMlException.class);
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        respuesta(
                                                "DESCONOCIDA",
                                                "0.2",
                                                "0.65",
                                                "0.15",
                                                "modelo-v1",
                                                FeatureSchemaV4Mapper.SCHEMA_VERSION)))
                .isInstanceOf(ModeloMlException.class);
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        respuesta(
                                                "ADECUADO",
                                                "0.2",
                                                "0.65",
                                                "0.15",
                                                " ",
                                                FeatureSchemaV4Mapper.SCHEMA_VERSION)))
                .isInstanceOf(ModeloMlException.class);
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        respuesta(
                                                "ADECUADO",
                                                "0.2",
                                                "0.65",
                                                "0.15",
                                                "modelo-v1",
                                                "variables-modelo-v3")))
                .isInstanceOf(ModeloMlException.class);
        assertThatThrownBy(
                        () ->
                                MlPredictResponseValidator.validar(
                                        new MlPredictResponse(
                                                "ADECUADO",
                                                null,
                                                "modelo-v1",
                                                FeatureSchemaV4Mapper.SCHEMA_VERSION)))
                .isInstanceOf(ModeloMlException.class);
    }

    static FeatureSchemaV4 schema() {
        var f = new LinkedHashMap<String, Object>();
        f.put("edad", 24);
        f.put("peso_kg", new BigDecimal("72.5"));
        f.put("altura_cm", new BigDecimal("174"));
        f.put("imc", null);
        f.put("promedio_cantidad_comidas", new BigDecimal("4.0"));
        f.put("promedio_consumo_agua", new BigDecimal("2.1"));
        f.put("proporcion_desayuno", new BigDecimal("0.85"));
        f.put("proporcion_snacks", new BigDecimal("0.40"));
        f.put("promedio_comidas_cocinadas", new BigDecimal("2.5"));
        f.put("proporcion_consume_suplementos", new BigDecimal("0.70"));
        f.put("suplementos_cantidad_aplicables", 2);
        f.put("suplementos_cantidad_activos_declarados", 2);
        return new FeatureSchemaV4(FeatureSchemaV4Mapper.SCHEMA_VERSION, f);
    }

    static MlPredictResponse respuesta(
            String clasificacion,
            String adecuado,
            String mejorable,
            String critico,
            String version,
            String schema) {
        return new MlPredictResponse(
                clasificacion,
                new MlProbabilidadesResponse(
                        new BigDecimal(adecuado),
                        new BigDecimal(mejorable),
                        new BigDecimal(critico)),
                version,
                schema);
    }
}
