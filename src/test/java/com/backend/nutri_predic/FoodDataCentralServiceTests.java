package com.backend.nutri_predic.alimentacion.service;

import static org.junit.jupiter.api.Assertions.*;
import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import com.sun.net.httpserver.HttpServer;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

class FoodDataCentralServiceTests {
    @Test void traduceBusquedaComunSinInventarComposicion() {
        assertEquals("rice cooked", FoodDataCentralService.traducirConsulta("Arroz cocido"));
        assertEquals("banana", FoodDataCentralService.traducirConsulta("Plátano"));
        assertEquals("chicken breast cooked", FoodDataCentralService.traducirConsulta("chicken breast cooked"));
    }
    private final JsonMapper mapper = JsonMapper.builder().build();
    @Test void searchSendsTranslatedQueryAsJson() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var request = new java.util.concurrent.atomic.AtomicReference<String>();
        var method = new java.util.concurrent.atomic.AtomicReference<String>();
        server.createContext("/foods/search", exchange -> {
            method.set(exchange.getRequestMethod());
            request.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            var bytes = ("{\"foods\":[" + DETAIL + "]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        try {
            var service = new FoodDataCentralService("test", RestClient.builder()
                    .baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build());
            assertEquals(1, service.buscar("arroz cocido").size());
            assertEquals("POST", method.get());
            var json = mapper.readTree(request.get());
            assertEquals("rice cooked", json.path("query").asText());
            assertEquals(3, json.path("dataType").size());
            assertEquals(15, json.path("pageSize").asInt());
        } finally { server.stop(0); }
    }
    private static final String DETAIL = """
        {"fdcId":123,"description":"Rice, cooked","foodNutrients":[
          {"nutrient":{"id":1003,"unitName":"g"},"amount":2},
          {"nutrient":{"id":1004,"unitName":"g"},"amount":1},
          {"nutrient":{"id":1005,"unitName":"g"},"amount":28}]}
        """;
    @Test void readsDetailAndPreservesSource() {
        var food = FoodDataCentralService.leer(mapper.readTree(DETAIL));
        assertNotNull(food);
        assertEquals(0, food.proteinaG().compareTo(new BigDecimal("2")));
        assertTrue(food.fuente().contains("123/nutrients"));
    }
    @Test void readsSearchShapeAndRealZero() {
        var food = FoodDataCentralService.leer(mapper.readTree("""
            {"fdcId":456,"description":"Food","foodNutrients":[
              {"nutrientId":1003,"unitName":"G","value":0},
              {"nutrientId":1004,"unitName":"G","value":1},
              {"nutrientId":1005,"unitName":"G","value":20}]}
            """));
        assertNotNull(food);
        assertEquals(0, food.proteinaG().signum());
    }
    @Test void rejectsMissingAndWrongUnits() {
        assertNull(FoodDataCentralService.leer(mapper.readTree(DETAIL.replace("\"amount\":2}", "\"amount\":null}"))));
        assertNull(FoodDataCentralService.leer(mapper.readTree(DETAIL.replace("\"unitName\":\"g\"", "\"unitName\":\"mg\""))));
    }
    @Test void scalesOnServerAndSavesProvenance() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/food/123", exchange -> {
            var bytes = DETAIL.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        try {
            var service = new FoodDataCentralService("test", RestClient.builder()
                    .baseUrl("http://127.0.0.1:" + server.getAddress().getPort()).build());
            var record = service.registro(123, new BigDecimal("150"), MomentoComida.ALMUERZO);
            assertEquals(0, record.proteinaG().compareTo(new BigDecimal("3")));
            assertEquals(0, record.carbohidratosG().compareTo(new BigDecimal("42")));
            assertEquals(0, record.grasasG().compareTo(new BigDecimal("1.5")));
            assertEquals("G", record.unidadCodigo());
            assertTrue(record.nombreAlimento().endsWith("[USDA:123]"));
            assertThrows(ResponseStatusException.class, () -> service.registro(123, BigDecimal.ZERO, MomentoComida.CENA));
        } finally { server.stop(0); }
    }
}
