package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.backend.nutri_predic.ml.client.HttpModeloMlClient;
import com.backend.nutri_predic.ml.config.ModeloMlProperties;
import com.backend.nutri_predic.ml.dto.MlPredictRequest;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpModeloMlClientTests {
    @Test
    void enviaPostPredictYDeserializaRespuestaValida() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("http://ml.test/predict"))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(
                        content()
                                .json(
                                        "{\"schemaVersion\":\"variables-modelo-v4\",\"features\":{\"edad\":24,\"peso_kg\":72.5,\"altura_cm\":174,\"promedio_cantidad_comidas\":4.0,\"promedio_consumo_agua\":2.1,\"proporcion_desayuno\":0.85,\"proporcion_snacks\":0.40,\"promedio_comidas_cocinadas\":2.5,\"proporcion_consume_suplementos\":0.70,\"suplementos_cantidad_aplicables\":2,\"suplementos_cantidad_activos_declarados\":2}}"))
                .andRespond(
                        withSuccess(
                                "{\"clasificacion\":\"MEJORABLE\",\"probabilidades\":{\"ADECUADO\":0.2,\"MEJORABLE\":0.65,\"CRITICO\":0.15},\"modelVersion\":\"modelo-v1\",\"schemaVersion\":\"variables-modelo-v4\"}",
                                MediaType.APPLICATION_JSON));

        var response =
                cliente(builder).predecir(MlPredictRequest.from(MlPredictContractTests.schema()));
        assertThat(response.clasificacion()).isEqualTo("MEJORABLE");
        server.verify();
    }

    @Test
    void erroresHttpTimeoutYJsonInvalidoNoFabricanRespuesta() {
        RestClient.Builder errorBuilder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer errorServer = MockRestServiceServer.bindTo(errorBuilder).build();
        errorServer.expect(requestTo("http://ml.test/predict")).andRespond(withServerError());
        assertThatThrownBy(
                        () ->
                                cliente(errorBuilder)
                                        .predecir(
                                                MlPredictRequest.from(
                                                        MlPredictContractTests.schema())))
                .isInstanceOf(ModeloMlException.class);
        RestClient.Builder timeoutBuilder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer timeoutServer = MockRestServiceServer.bindTo(timeoutBuilder).build();
        timeoutServer
                .expect(requestTo("http://ml.test/predict"))
                .andRespond(withException(new SocketTimeoutException("timeout")));
        assertThatThrownBy(
                        () ->
                                cliente(timeoutBuilder)
                                        .predecir(
                                                MlPredictRequest.from(
                                                        MlPredictContractTests.schema())))
                .isInstanceOf(ModeloMlException.class);
        RestClient.Builder jsonBuilder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer jsonServer = MockRestServiceServer.bindTo(jsonBuilder).build();
        jsonServer
                .expect(requestTo("http://ml.test/predict"))
                .andRespond(withSuccess("{", MediaType.APPLICATION_JSON));
        assertThatThrownBy(
                        () ->
                                cliente(jsonBuilder)
                                        .predecir(
                                                MlPredictRequest.from(
                                                        MlPredictContractTests.schema())))
                .isInstanceOf(ModeloMlException.class);
    }

    @Test
    void contratoV5ConservaMetadataDeInferencia() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ml.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("http://ml.test/predict"))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"schemaVersion\":\"variables-modelo-v5\"}"))
                .andRespond(
                        withSuccess(
                                """
                        {"clasificacion":"MEJORABLE",
                         "probabilidades":{"ADECUADO":0.2,"MEJORABLE":0.65,"CRITICO":0.15},
                         "modelVersion":"modelo-v5","schemaVersion":"variables-modelo-v5",
                         "inferenceMs":3.75,"inferredAt":"2026-08-30T12:00:00.123Z"}
                        """,
                                MediaType.APPLICATION_JSON));
        var features = new LinkedHashMap<String, Object>();
        FeatureSchemaV5Mapper.FEATURE_NAMES.forEach(nombre -> features.put(nombre, null));
        features.put("edad", 29);

        var response =
                cliente(builder)
                        .predecir(
                                MlPredictRequest.from(
                                        new FeatureSchemaV5(
                                                FeatureSchemaV5Mapper.SCHEMA_VERSION, features)));

        assertThat(response.inferenceMs()).isEqualByComparingTo(new BigDecimal("3.75"));
        assertThat(response.inferredAt()).isEqualTo(Instant.parse("2026-08-30T12:00:00.123Z"));
        server.verify();
    }

    private HttpModeloMlClient cliente(RestClient.Builder builder) {
        var properties = new ModeloMlProperties();
        properties.setBaseUrl("http://ml.test");
        properties.setPredictPath("/predict");
        properties.setTimeout(Duration.ofSeconds(1));
        return new HttpModeloMlClient(builder.build(), properties);
    }
}
