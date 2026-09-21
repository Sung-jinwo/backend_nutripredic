package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.RegistroAlimentoRequest;
import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

/** USDA CC0 data. Remote amounts are per 100 g; never substitute missing values with zero. */
@Service
public class FoodDataCentralService {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FoodDataCentralService.class);
    public record Food(long fdcId, String nombre, BigDecimal proteinaG,
            BigDecimal carbohidratosG, BigDecimal grasasG, String fuente) {}

    private final RestClient client;
    private final String apiKey;

    @org.springframework.beans.factory.annotation.Autowired
    public FoodDataCentralService(@Value("${app.food-data-central.api-key:DEMO_KEY}") String apiKey) {
        this(apiKey, crearCliente());
    }

    FoodDataCentralService(String apiKey, RestClient client) {
        this.apiKey = apiKey;
        this.client = client;
    }

    private static RestClient crearCliente() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(12));
        return RestClient.builder().baseUrl("https://api.nal.usda.gov/fdc/v1")
                .requestFactory(factory).build();
    }

    public List<Food> buscar(String query) {
        if (query == null || query.trim().length() < 2 || query.length() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe entre 2 y 100 caracteres.");
        var json = consultar("/foods/search", traducirConsulta(query));
        var foods = new ArrayList<Food>();
        for (var item : json.path("foods")) {
            var food = leer(item);
            if (food != null) foods.add(food);
        }
        return foods;
    }

    static String traducirConsulta(String query) {
        var palabras = java.util.Map.ofEntries(
                java.util.Map.entry("arroz", "rice"), java.util.Map.entry("pollo", "chicken"),
                java.util.Map.entry("pechuga", "breast"), java.util.Map.entry("huevo", "egg"),
                java.util.Map.entry("papa", "potato"), java.util.Map.entry("patata", "potato"),
                java.util.Map.entry("platano", "banana"), java.util.Map.entry("leche", "milk"),
                java.util.Map.entry("avena", "oats"), java.util.Map.entry("pan", "bread"),
                java.util.Map.entry("cocido", "cooked"), java.util.Map.entry("cocida", "cooked"),
                java.util.Map.entry("sancochado", "boiled"), java.util.Map.entry("sancochada", "boiled"),
                java.util.Map.entry("hervido", "boiled"), java.util.Map.entry("crudo", "raw"),
                java.util.Map.entry("cruda", "raw"), java.util.Map.entry("blanco", "white"),
                java.util.Map.entry("integral", "whole grain"), java.util.Map.entry("con", "with"));
        var texto = java.text.Normalizer.normalize(query.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT);
        return java.util.Arrays.stream(texto.split("\\s+")).map(p -> palabras.getOrDefault(p, p))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    public RegistroAlimentoRequest registro(long id, BigDecimal gramos, MomentoComida momento) {
        if (id <= 0 || gramos == null || gramos.signum() <= 0 || gramos.compareTo(new BigDecimal("10000")) > 0 || momento == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un alimento y una cantidad válida en gramos (hasta 10000).");
        var food = leer(consultar("/food/" + id, null));
        if (food == null)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "USDA no informa los tres macronutrientes; utiliza el registro manual con información verificada.");
        var factor = gramos.divide(new BigDecimal("100"));
        // Source identifier is part of the saved daily snapshot, not a mutable client estimate.
        var suffix = " [USDA:" + food.fdcId() + "]";
        var name = food.nombre().substring(0, Math.min(food.nombre().length(), 200 - suffix.length())) + suffix;
        return new RegistroAlimentoRequest(null, name, gramos, "G", momento,
                escalar(food.proteinaG(), factor), escalar(food.carbohidratosG(), factor), escalar(food.grasasG(), factor));
    }

    static BigDecimal escalar(BigDecimal valor, BigDecimal factor) {
        return valor.multiply(factor).setScale(4, RoundingMode.HALF_UP);
    }

    static Food leer(JsonNode item) {
        long id = item.path("fdcId").asLong(0);
        String nombre = item.path("description").asText("");
        BigDecimal protein = null, carbs = null, fat = null;
        for (var entry : item.path("foodNutrients")) {
            var nutrient = entry.path("nutrient");
            int nutrientId = nutrient.isMissingNode() ? entry.path("nutrientId").asInt(0) : nutrient.path("id").asInt(0);
            String unit = nutrient.isMissingNode() ? entry.path("unitName").asText("") : nutrient.path("unitName").asText("");
            var amount = nutrient.isMissingNode() ? entry.path("value") : entry.path("amount");
            if (!unit.equalsIgnoreCase("g") || !amount.isNumber() || amount.decimalValue().signum() < 0) continue;
            switch (nutrientId) {
                case 1003 -> protein = amount.decimalValue();
                case 1004 -> fat = amount.decimalValue();
                case 1005 -> carbs = amount.decimalValue();
                default -> { }
            }
        }
        if (id <= 0 || nombre.isBlank() || protein == null || carbs == null || fat == null) return null;
        return new Food(id, nombre, protein, carbs, fat, "https://fdc.nal.usda.gov/food-details/" + id + "/nutrients");
    }

    private JsonNode consultar(String path, String query) {
        try {
            java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uri = builder -> {
                builder.path(path).queryParam("api_key", apiKey);
                return builder.build();
            };
            var result = query == null ? client.get().uri(uri).retrieve().body(JsonNode.class)
                    : client.post().uri(uri).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .body(java.util.Map.of("query", query, "pageSize", 15,
                                    "dataType", List.of("Foundation", "SR Legacy", "Survey (FNDDS)")))
                            .retrieve().body(JsonNode.class);
            if (result == null) throw new IllegalStateException("Empty response");
            return result;
        } catch (Exception ex) {
            Throwable causa = ex;
            while (causa.getCause() != null) causa = causa.getCause();
            Integer status = ex instanceof org.springframework.web.client.RestClientResponseException http
                    ? http.getStatusCode().value() : null;
            LOG.warn("USDA consulta fallida: tipo={}, causa={}, estadoHttp={}",
                    ex.getClass().getSimpleName(), causa.getClass().getSimpleName(), status);
            if (status != null && status == 429)
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Se agotó la cuota de USDA. Configura una clave gratuita propia en el backend o vuelve a intentar más tarde.");
            // Do not include remote URL/exception: API key must never reach logs or responses.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar USDA. Comprueba la clave o el límite de consultas; puedes registrar el alimento manualmente.");
        }
    }
}
