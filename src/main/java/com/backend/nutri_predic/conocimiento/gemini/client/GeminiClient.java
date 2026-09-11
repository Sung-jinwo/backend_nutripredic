package com.backend.nutri_predic.conocimiento.gemini.client;

import com.backend.nutri_predic.conocimiento.gemini.config.GeminiProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GeminiClient {
    enum Stage {
        BUILD_REQUEST,
        SEND_REQUEST,
        RECEIVE_RESPONSE,
        READ_BODY,
        PARSE_ENVELOPE,
        EXTRACT_CANDIDATES,
        PARSE_GENERATED_JSON,
        VALIDATE_GENERATED_QUESTIONS
    }

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private final GeminiProperties properties;
    private final RestClient client;
    private final boolean retriesEnabled;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public GeminiClient(RestClient.Builder b, GeminiProperties p) {
        properties = p;
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(p.getTimeout());
        f.setReadTimeout(p.getTimeout());
        client = b.baseUrl(p.getBaseUrl()).requestFactory(f).build();
        retriesEnabled = true;
    }

    public GeminiClient(RestClient c, GeminiProperties p) {
        client = c;
        properties = p;
        retriesEnabled = false;
    }

    public List<Respuesta.Pregunta> generar(Solicitud solicitud) {
        int intentos = retriesEnabled ? 2 : 1;
        for (int intento = 1; intento <= intentos; intento++) {
            try {
                return generarUnaVez(solicitud);
            } catch (GeminiClientException error) {
                if (intento == intentos || !reintentable(error)) throw error;
                log.warn("Gemini temporalmente no disponible; reintento {}/{}", intento + 1, intentos);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw error;
                }
            }
        }
        throw new IllegalStateException("No se ejecutó ningún intento Gemini");
    }

    private boolean reintentable(GeminiClientException error) {
        Integer status = error.getHttpStatus();
        return status != null && (status == 429 || status >= 500)
                || "GEMINI_TIMEOUT".equals(error.getCodigoTecnico())
                || "GEMINI_IO_ERROR".equals(error.getCodigoTecnico());
    }

    private List<Respuesta.Pregunta> generarUnaVez(Solicitud solicitud) {
        if (properties.isMockEnabled()) return mock(solicitud);
        var stage = new AtomicReference<>(Stage.BUILD_REQUEST);
        try {
            byte[] request =
                    mapper.writeValueAsBytes(
                            Map.of(
                                    "contents",
                                    List.of(
                                            Map.of(
                                                    "parts",
                                                    List.of(Map.of("text", prompt(solicitud))))),
                                    "generationConfig",
                                    Map.of(
                                            "thinkingConfig",
                                            Map.of("thinkingLevel", "minimal"),
                                            "responseMimeType",
                                            "application/json",
                                            "responseSchema",
                                            schema())));
            stage.set(Stage.SEND_REQUEST);
            RawResponse raw =
                    client.post()
                            .uri("/v1beta/models/{model}:generateContent", properties.getModel())
                            .header("x-goog-api-key", properties.getApiKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(request)
                            .exchange(
                                    (req, res) -> {
                                        stage.set(Stage.RECEIVE_RESPONSE);
                                        int status = res.getStatusCode().value();
                                        String contentType =
                                                res.getHeaders().getContentType() == null
                                                        ? null
                                                        : res.getHeaders()
                                                                .getContentType()
                                                                .toString();
                                        stage.set(Stage.READ_BODY);
                                        try {
                                            return new RawResponse(
                                                    status,
                                                    contentType,
                                                    res.getBody().readAllBytes());
                                        } catch (IOException ex) {
                                            throw new BodyReadException(status, contentType, ex);
                                        }
                                    });
            return procesar(raw, stage);
        } catch (GeminiClientException ex) {
            throw ex;
        } catch (BodyReadException ex) {
            var d =
                    new GeminiResponseDiagnostics(
                            ex.status,
                            ex.contentType,
                            0,
                            0,
                            List.of(),
                            false,
                            0,
                            false,
                            false,
                            false,
                            null,
                            Stage.READ_BODY.name(),
                            null,
                            null);
            throw error("GEMINI_BODY_READ_ERROR", ex.status, d, Stage.READ_BODY, ex);
        } catch (ResourceAccessException ex) {
            Throwable root = root(ex);
            String code =
                    root instanceof SocketTimeoutException
                            ? "GEMINI_TIMEOUT"
                            : root instanceof SSLException
                                    ? "GEMINI_TLS_ERROR"
                                    : root instanceof IOException
                                            ? "GEMINI_IO_ERROR"
                                            : "GEMINI_RESOURCE_ACCESS_ERROR";
            throw error(code, null, null, stage.get(), ex);
        } catch (RestClientException ex) {
            throw error("GEMINI_RESTCLIENT_ERROR", null, null, stage.get(), ex);
        } catch (JsonProcessingException ex) {
            throw error("GEMINI_REQUEST_SERIALIZATION_ERROR", null, null, stage.get(), ex);
        } catch (Exception ex) {
            throw error("GEMINI_CLIENT_UNEXPECTED", null, null, stage.get(), ex);
        }
    }

    private List<Respuesta.Pregunta> procesar(RawResponse raw, AtomicReference<Stage> stage) {
        if (raw == null)
            throw error(
                    "GEMINI_RESTCLIENT_ERROR",
                    null,
                    null,
                    stage.get(),
                    new IllegalStateException());
        GeminiResponseDiagnostics base =
                new GeminiResponseDiagnostics(
                        raw.status,
                        raw.contentType,
                        0,
                        0,
                        List.of(),
                        false,
                        raw.body == null ? 0 : raw.body.length,
                        false,
                        false,
                        false,
                        null,
                        Stage.READ_BODY.name(),
                        null,
                        null);
        if (raw.status < 200 || raw.status >= 300) throw httpError(raw, base);
        if (raw.body == null || raw.body.length == 0)
            throw error(
                    "GEMINI_RESPONSE_EMPTY",
                    raw.status,
                    base,
                    Stage.READ_BODY,
                    new EmptyBodyException());
        stage.set(Stage.PARSE_ENVELOPE);
        ApiResponse envelope;
        try {
            envelope =
                    mapper.readValue(
                            new String(raw.body, StandardCharsets.UTF_8), ApiResponse.class);
        } catch (JsonProcessingException ex) {
            throw error(
                    "GEMINI_RESPONSE_STRUCTURE_INVALID",
                    raw.status,
                    withStage(base, Stage.PARSE_ENVELOPE),
                    Stage.PARSE_ENVELOPE,
                    ex);
        }
        stage.set(Stage.EXTRACT_CANDIDATES);
        return interpretar(envelope, raw.status, raw.contentType, stage);
    }

    private List<Respuesta.Pregunta> interpretar(
            ApiResponse r, int status, String ct, AtomicReference<Stage> stage) {
        int cc = r == null || r.candidates() == null ? 0 : r.candidates().size();
        Candidate c = cc == 0 ? null : r.candidates().getFirst();
        List<Part> parts =
                c == null || c.content() == null || c.content().parts() == null
                        ? List.of()
                        : c.content().parts();
        String raw =
                parts.stream().map(Part::text).filter(Objects::nonNull).reduce("", String::concat);
        String trimmed = raw.strip().replaceFirst("^\\uFEFF", "");
        boolean fence = trimmed.startsWith("```");
        var d =
                new GeminiResponseDiagnostics(
                        status,
                        ct,
                        cc,
                        parts.size(),
                        parts.stream().map(p -> p.text() == null ? "NON_TEXT" : "TEXT").toList(),
                        !raw.isBlank(),
                        raw.length(),
                        trimmed.startsWith("{"),
                        trimmed.startsWith("["),
                        fence,
                        c == null ? null : c.finishReason(),
                        Stage.EXTRACT_CANDIDATES.name(),
                        r == null || r.error() == null ? null : r.error().status(),
                        r == null || r.error() == null ? null : r.error().code());
        if (cc == 0 || parts.isEmpty())
            throw error(
                    "GEMINI_RESPONSE_EMPTY",
                    status,
                    d,
                    Stage.EXTRACT_CANDIDATES,
                    new EmptyBodyException());
        if (raw.isBlank() || r.error() != null)
            throw error(
                    "GEMINI_RESPONSE_STRUCTURE_INVALID",
                    status,
                    d,
                    Stage.EXTRACT_CANDIDATES,
                    new IllegalStateException());
        String json = normalizar(trimmed);
        stage.set(Stage.PARSE_GENERATED_JSON);
        d =
                new GeminiResponseDiagnostics(
                        status,
                        ct,
                        cc,
                        parts.size(),
                        d.partTypes(),
                        true,
                        raw.length(),
                        json.startsWith("{"),
                        json.startsWith("["),
                        fence,
                        d.finishReason(),
                        Stage.PARSE_GENERATED_JSON.name(),
                        d.apiErrorStatus(),
                        d.apiErrorCode());
        try {
            var respuesta = mapper.readValue(json, Respuesta.class);
            if (respuesta == null || respuesta.preguntas() == null)
                throw error(
                        "GEMINI_SCHEMA_INVALID",
                        status,
                        d,
                        Stage.PARSE_GENERATED_JSON,
                        new IllegalStateException());
            return respuesta.preguntas();
        } catch (JsonProcessingException ex) {
            throw error("GEMINI_JSON_INVALID", status, d, Stage.PARSE_GENERATED_JSON, ex);
        }
    }

    private GeminiClientException httpError(RawResponse raw, GeminiResponseDiagnostics base) {
        String status = null;
        Integer code = null;
        try {
            var root = mapper.readTree(new String(raw.body, StandardCharsets.UTF_8)).path("error");
            status = root.path("status").isTextual() ? root.path("status").asText() : null;
            code = root.path("code").isInt() ? root.path("code").asInt() : null;
        } catch (Exception ignored) {
        }
        var d =
                new GeminiResponseDiagnostics(
                        raw.status,
                        raw.contentType,
                        0,
                        0,
                        List.of(),
                        false,
                        0,
                        false,
                        false,
                        false,
                        null,
                        Stage.RECEIVE_RESPONSE.name(),
                        status,
                        code);
        return error(
                httpCode(raw.status),
                raw.status,
                d,
                Stage.RECEIVE_RESPONSE,
                new HttpStatusException());
    }

    private GeminiClientException error(
            String code, Integer status, GeminiResponseDiagnostics d, Stage stage, Throwable ex) {
        String type = ex == null ? null : ex.getClass().getSimpleName();
        String root = root(ex) == null ? null : root(ex).getClass().getSimpleName();
        log.warn(
                "Gemini client fallo codigo={} httpStatus={} etapa={} tipoExcepcion={} causaRaiz={} contentType={} candidateCount={} partCount={} hasText={} textLength={} finishReason={}",
                code,
                status,
                stage,
                type,
                root,
                d == null ? null : d.contentType(),
                d == null ? null : d.candidateCount(),
                d == null ? null : d.partCount(),
                d == null ? null : d.hasText(),
                d == null ? null : d.textLength(),
                d == null ? null : d.finishReason());
        return new GeminiClientException(
                code,
                status,
                d,
                stage.name(),
                type + (root == null || root.equals(type) ? "" : "/" + root));
    }

    private Throwable root(Throwable ex) {
        if (ex == null) return null;
        Throwable r = ex;
        while (r.getCause() != null && r.getCause() != r) r = r.getCause();
        return r;
    }

    private GeminiResponseDiagnostics withStage(GeminiResponseDiagnostics d, Stage s) {
        return new GeminiResponseDiagnostics(
                d.httpStatus(),
                d.contentType(),
                d.candidateCount(),
                d.partCount(),
                d.partTypes(),
                d.hasText(),
                d.textLength(),
                d.startsWithBrace(),
                d.startsWithBracket(),
                d.hasMarkdownFence(),
                d.finishReason(),
                s.name(),
                d.apiErrorStatus(),
                d.apiErrorCode());
    }

    private String normalizar(String v) {
        String x = v.strip().replaceFirst("^\\uFEFF", "");
        if (x.startsWith("```")) {
            int nl = x.indexOf('\n');
            int end = x.lastIndexOf("```");
            if (nl > 0 && end > nl) x = x.substring(nl + 1, end).strip();
        }
        return x;
    }

    private String httpCode(int s) {
        if (s == 400) return "GEMINI_HTTP_400";
        if (s == 401) return "GEMINI_HTTP_401";
        if (s == 403) return "GEMINI_HTTP_403";
        if (s == 404) return "GEMINI_HTTP_404";
        if (s == 429) return "GEMINI_HTTP_429";
        return s >= 500 ? "GEMINI_HTTP_5XX" : "GEMINI_HTTP_ERROR";
    }

    private List<Respuesta.Pregunta> mock(Solicitud s) {
        String t = s.temasPermitidos().getFirst();
        return java.util.stream.IntStream.range(0, s.cantidad())
                .mapToObj(
                        i ->
                                new Respuesta.Pregunta(
                                        t,
                                        "General",
                                        s.dificultadPermitida(),
                                        "Pregunta adaptativa de prueba " + (i + 1) + " sobre " + t,
                                        List.of(
                                                new Opcion("A", "Opción A"),
                                                new Opcion("B", "Opción B"),
                                                new Opcion("C", "Opción C"),
                                                new Opcion("D", "Opción D")),
                                        "B",
                                        "Explicación de prueba"))
                .toList();
    }

    private String prompt(Solicitud s) {
        return "Genera exactamente "
                + s.cantidad()
                + " preguntas educativas de opción múltiple para una evaluación diaria. Cada pregunta vale 2 puntos. Temas permitidos: "
                + String.join(", ", s.temasPermitidos())
                + ". Dificultad obligatoria: "
                + s.dificultadPermitida()
                + ". Objetivo nutricional y físico declarado por el cliente: "
                + textoSeguro(s.objetivoCliente())
                + ". Clasificación producida por el modelo predictivo: "
                + textoSeguro(s.clasificacionPredictiva())
                + ". Plan nutricional diario calculado por el backend (contexto educativo, no lo modifiques): "
                + textoSeguro(s.contextoPlanNutricional())
                + ". Errores educativos de la evaluación anterior: "
                + textoSeguro(s.contextoErroresPrevios())
                + ". Da prioridad a los temas fallados, pero redacta preguntas nuevas y no repitas literalmente las anteriores."
                + " Adapta el contenido educativo al objetivo y a la clasificación, sin afirmar diagnósticos."
                + ". No incluyas datos personales ni recomendaciones clínicas.";
    }

    private String textoSeguro(String valor) {
        return valor == null || valor.isBlank() ? "NO_DISPONIBLE" : valor.trim();
    }

    private Map<String, Object> schema() {
        var o =
                Map.<String, Object>of(
                        "type",
                        "OBJECT",
                        "properties",
                        Map.of(
                                "codigo",
                                Map.of("type", "STRING"),
                                "texto",
                                Map.of("type", "STRING")),
                        "required",
                        List.of("codigo", "texto"));
        var p =
                Map.<String, Object>of(
                        "type",
                        "OBJECT",
                        "properties",
                        Map.of(
                                "tema",
                                Map.of("type", "STRING"),
                                "subtema",
                                Map.of("type", "STRING"),
                                "dificultad",
                                Map.of("type", "STRING"),
                                "enunciado",
                                Map.of("type", "STRING"),
                                "opciones",
                                Map.of("type", "ARRAY", "items", o),
                                "respuestaCorrecta",
                                Map.of("type", "STRING"),
                                "explicacion",
                                Map.of("type", "STRING")),
                        "required",
                        List.of(
                                "tema",
                                "subtema",
                                "dificultad",
                                "enunciado",
                                "opciones",
                                "respuestaCorrecta",
                                "explicacion"));
        return Map.of(
                "type",
                "OBJECT",
                "properties",
                Map.of("preguntas", Map.of("type", "ARRAY", "items", p)),
                "required",
                List.of("preguntas"));
    }

    public record Solicitud(
            List<String> temasPermitidos,
            String dificultadPermitida,
            int cantidad,
            String objetivoCliente,
            String clasificacionPredictiva,
            String contextoPlanNutricional,
            String contextoErroresPrevios) {
        public Solicitud(List<String> temasPermitidos, String dificultadPermitida, int cantidad) {
            this(temasPermitidos, dificultadPermitida, cantidad, null, null, null, null);
        }
        public Solicitud(List<String> temasPermitidos, String dificultadPermitida, int cantidad,
                String objetivoCliente, String clasificacionPredictiva, String contextoPlanNutricional) {
            this(temasPermitidos, dificultadPermitida, cantidad, objetivoCliente,
                    clasificacionPredictiva, contextoPlanNutricional, null);
        }
    }

    public record Respuesta(List<Pregunta> preguntas) {
        public record Pregunta(
                String tema,
                String subtema,
                String dificultad,
                String enunciado,
                List<Opcion> opciones,
                String respuestaCorrecta,
                String explicacion) {}
    }

    public record Opcion(String codigo, String texto) {}

    private record RawResponse(int status, String contentType, byte[] body) {}

    private static class BodyReadException extends RuntimeException {
        final int status;
        final String contentType;

        BodyReadException(int s, String c, Throwable x) {
            super(x);
            status = s;
            contentType = c;
        }
    }

    private static class EmptyBodyException extends RuntimeException {}

    private static class HttpStatusException extends RuntimeException {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ApiResponse(List<Candidate> candidates, ApiError error) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content, String finishReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ApiError(Integer code, String status) {}
}
