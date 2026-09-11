package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.backend.nutri_predic.conocimiento.gemini.client.GeminiClient;
import com.backend.nutri_predic.conocimiento.gemini.client.GeminiClientException;
import com.backend.nutri_predic.conocimiento.gemini.config.GeminiProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class GeminiClientTests {
    private static final GeminiClient.Solicitud SOLICITUD =
            new GeminiClient.Solicitud(List.of("PROTEINA"), "MEDIA", 2);

    @Test
    void respuestaValidaSeConvierteEnPreguntas() {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andExpect(method(POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.generationConfig.thinkingConfig.thinkingLevel")
                                .value("minimal"))
                .andExpect(
                        jsonPath("$.generationConfig.thinkingConfig.thinkingBudget").doesNotExist())
                .andExpect(
                        jsonPath("$.generationConfig.responseMimeType").value("application/json"))
                .andExpect(jsonPath("$.generationConfig.responseSchema").exists())
                .andRespond(withSuccess(respuestaValida(), MediaType.APPLICATION_JSON));

        var respuesta = fixture.client.generar(SOLICITUD);

        assertThat(respuesta).hasSize(2);
        fixture.server.verify();
    }

    @Test
    void configuracionEfectivaConstruyeUriGeminiSinClaveEnQueryString() {
        RestClient.Builder builder =
                RestClient.builder().baseUrl("https://generativelanguage.googleapis.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var properties = new GeminiProperties();
        properties.setBaseUrl("https://generativelanguage.googleapis.com");
        properties.setApiKey("test-key");
        properties.setModel("gemini-3.5-flash-lite");
        properties.setTimeout(Duration.parse("PT30S"));
        properties.setMockEnabled(false);
        server.expect(
                        requestTo(
                                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent"))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andRespond(withSuccess(respuestaValida(), MediaType.APPLICATION_JSON));

        assertThat(new GeminiClient(builder.build(), properties).generar(SOLICITUD)).hasSize(2);
        assertThat(properties.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        server.verify();
    }

    @Test
    void candidatesYPartsVaciosSeReportanComoRespuestaVacia() {
        assertCodigo("{\"candidates\":[]}", "GEMINI_RESPONSE_EMPTY", 200);
        assertCodigo(
                "{\"candidates\":[{\"content\":{\"parts\":[]}}]}", "GEMINI_RESPONSE_EMPTY", 200);
    }

    @Test
    void bodyHttpVacioConservaStatusYEtapaLectura() {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> fixture.client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> {
                            assertThat(error.getCodigoTecnico()).isEqualTo("GEMINI_RESPONSE_EMPTY");
                            assertThat(error.getHttpStatus()).isEqualTo(200);
                            assertThat(error.getEtapaError()).isEqualTo("READ_BODY");
                        });
    }

    @Test
    void excepcionLeyendoBodyConservaHttp200() {
        var response =
                new MockClientHttpResponse(
                        new InputStream() {
                            @Override
                            public int read() throws IOException {
                                throw new IOException("read");
                            }
                        },
                        HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var client =
                rawClient(
                        (uri, method) -> {
                            var request = new MockClientHttpRequest(method, uri);
                            request.setResponse(response);
                            return request;
                        });
        assertThatThrownBy(() -> client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> {
                            assertThat(error.getCodigoTecnico())
                                    .isEqualTo("GEMINI_BODY_READ_ERROR");
                            assertThat(error.getHttpStatus()).isEqualTo(200);
                            assertThat(error.getEtapaError()).isEqualTo("READ_BODY");
                            assertThat(error.getTipoExcepcionSeguro())
                                    .contains("BodyReadException")
                                    .contains("IOException");
                        });
    }

    @Test
    void resourceAccessRestClientEInesperadaNoSeColapsan() {
        assertFalloPropiedad(
                new ResourceAccessException("x", new IOException("io")), "GEMINI_IO_ERROR");
        assertFalloPropiedad(new RestClientException("x"), "GEMINI_RESTCLIENT_ERROR");
        assertFalloPropiedad(new IllegalArgumentException("x"), "GEMINI_CLIENT_UNEXPECTED");
    }

    @Test
    void textoNoJsonSeReportaComoRespuestaInvalida() {
        assertCodigo(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"no-json\"}]}}]}",
                "GEMINI_JSON_INVALID",
                200);
    }

    @Test
    void jsonConFenceSeNormalizaDeFormaAcotada() {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(
                        withSuccess(
                                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"```json\\n{\\\"preguntas\\\":[]}\\n```\"}]},\"finishReason\":\"STOP\"}]}",
                                MediaType.APPLICATION_JSON));
        assertThat(fixture.client.generar(SOLICITUD)).isEmpty();
    }

    @Test
    void jsonInvalidoConHttp200ConservaStatusYDiagnosticoSanitizado() {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(
                        withSuccess(
                                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"no-json\"}]},\"finishReason\":\"STOP\"}]}",
                                MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> fixture.client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> {
                            assertThat(error.getCodigoTecnico()).isEqualTo("GEMINI_JSON_INVALID");
                            assertThat(error.getHttpStatus()).isEqualTo(200);
                            assertThat(error.getDiagnostics().finishReason()).isEqualTo("STOP");
                            assertThat(error.getDiagnostics().textLength()).isEqualTo(7);
                            assertThat(error.getDiagnostics().hasMarkdownFence()).isFalse();
                        });
    }

    @Test
    void envelopeRealistaConCamposAdicionalesNoFallaPorMezclaDeJackson() {
        var fixture = fixture();
        String base = respuestaValida();
        String body =
                base.substring(0, base.length() - 1)
                        + ",\"usageMetadata\":{\"promptTokenCount\":10},\"modelVersion\":\"3.5-flash-lite-07-2026\"}";
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThat(fixture.client.generar(SOLICITUD)).hasSize(2);
    }

    @Test
    void estadosHttpSeClasificanSinExponerDetalles() {
        assertHttp(HttpStatus.BAD_REQUEST, "GEMINI_HTTP_400");
        assertHttp(HttpStatus.UNAUTHORIZED, "GEMINI_HTTP_401");
        assertHttp(HttpStatus.FORBIDDEN, "GEMINI_HTTP_403");
        assertHttp(HttpStatus.NOT_FOUND, "GEMINI_HTTP_404");
        assertHttp(HttpStatus.TOO_MANY_REQUESTS, "GEMINI_HTTP_429");
        assertHttp(HttpStatus.INTERNAL_SERVER_ERROR, "GEMINI_HTTP_5XX");
        assertHttp(HttpStatus.SERVICE_UNAVAILABLE, "GEMINI_HTTP_5XX");
    }

    @Test
    void timeoutSeClasificaSinRespuestaFabricada() {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(withException(new SocketTimeoutException("timeout")));

        assertThatThrownBy(() -> fixture.client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> assertThat(error.getCodigoTecnico()).isEqualTo("GEMINI_TIMEOUT"));
    }

    private void assertHttp(HttpStatus status, String codigo) {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(withStatus(status));
        assertThatThrownBy(() -> fixture.client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> {
                            assertThat(error.getCodigoTecnico()).isEqualTo(codigo);
                            assertThat(error.getHttpStatus()).isEqualTo(status.value());
                        });
    }

    private void assertCodigo(String body, String codigo, Integer status) {
        var fixture = fixture();
        fixture.server
                .expect(requestTo("http://gemini.test/v1beta/models/test-model:generateContent"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> fixture.client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> {
                            assertThat(error.getCodigoTecnico()).isEqualTo(codigo);
                            assertThat(error.getHttpStatus()).isEqualTo(status);
                        });
    }

    private Fixture fixture() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://gemini.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var properties = new GeminiProperties();
        properties.setBaseUrl("http://gemini.test");
        properties.setApiKey("test-key");
        properties.setModel("test-model");
        properties.setTimeout(Duration.ofSeconds(1));
        properties.setMockEnabled(false);
        return new Fixture(new GeminiClient(builder.build(), properties), server);
    }

    private GeminiClient rawClient(
            org.springframework.http.client.ClientHttpRequestFactory factory) {
        var properties = properties();
        return new GeminiClient(
                RestClient.builder().baseUrl("http://gemini.test").requestFactory(factory).build(),
                properties);
    }

    private void assertFalloPropiedad(RuntimeException thrown, String codigo) {
        var properties = org.mockito.Mockito.mock(GeminiProperties.class);
        org.mockito.Mockito.when(properties.getModel()).thenThrow(thrown);
        var client = new GeminiClient(RestClient.create(), properties);
        assertThatThrownBy(() -> client.generar(SOLICITUD))
                .isInstanceOfSatisfying(
                        GeminiClientException.class,
                        error -> {
                            assertThat(error.getCodigoTecnico()).isEqualTo(codigo);
                            assertThat(error.getEtapaError()).isEqualTo("SEND_REQUEST");
                            assertThat(error.getTipoExcepcionSeguro()).isNotBlank();
                        });
    }

    private GeminiProperties properties() {
        var properties = new GeminiProperties();
        properties.setBaseUrl("http://gemini.test");
        properties.setApiKey("test-key");
        properties.setModel("test-model");
        properties.setTimeout(Duration.ofSeconds(1));
        properties.setMockEnabled(false);
        return properties;
    }

    private String respuestaValida() {
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\\"preguntas\\\":[{\\\"tema\\\":\\\"PROTEINA\\\",\\\"subtema\\\":\\\"General\\\",\\\"dificultad\\\":\\\"MEDIA\\\",\\\"enunciado\\\":\\\"Pregunta uno\\\",\\\"opciones\\\":[{\\\"codigo\\\":\\\"A\\\",\\\"texto\\\":\\\"A\\\"},{\\\"codigo\\\":\\\"B\\\",\\\"texto\\\":\\\"B\\\"},{\\\"codigo\\\":\\\"C\\\",\\\"texto\\\":\\\"C\\\"},{\\\"codigo\\\":\\\"D\\\",\\\"texto\\\":\\\"D\\\"}],\\\"respuestaCorrecta\\\":\\\"B\\\",\\\"explicacion\\\":\\\"Explicacion\\\"},{\\\"tema\\\":\\\"PROTEINA\\\",\\\"subtema\\\":\\\"General\\\",\\\"dificultad\\\":\\\"MEDIA\\\",\\\"enunciado\\\":\\\"Pregunta dos\\\",\\\"opciones\\\":[{\\\"codigo\\\":\\\"A\\\",\\\"texto\\\":\\\"A\\\"},{\\\"codigo\\\":\\\"B\\\",\\\"texto\\\":\\\"B\\\"},{\\\"codigo\\\":\\\"C\\\",\\\"texto\\\":\\\"C\\\"},{\\\"codigo\\\":\\\"D\\\",\\\"texto\\\":\\\"D\\\"}],\\\"respuestaCorrecta\\\":\\\"B\\\",\\\"explicacion\\\":\\\"Explicacion\\\"}]}\"}]}}]}";
    }

    private record Fixture(GeminiClient client, MockRestServiceServer server) {}
}
