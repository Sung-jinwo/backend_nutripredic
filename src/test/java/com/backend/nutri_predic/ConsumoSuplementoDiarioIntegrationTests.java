package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.repository.SnapshotEvaluacionConsumoRepository;
import com.backend.nutri_predic.alimentacion.habito.dto.HabitoUpdateRequest;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.alimentacion.habito.service.HabitoService;
import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.entity.PeriodoFrecuencia;
import com.backend.nutri_predic.suplemento.repository.SuplementoCatalogoRepository;
import com.backend.nutri_predic.suplemento.repository.SuplementoClienteRepository;
import com.backend.nutri_predic.suplemento.service.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ConsumoSuplementoDiarioIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired SuplementoService suplementos;
    @Autowired SuplementoClienteRepository habituales;
    @Autowired SuplementoCatalogoRepository catalogoRepo;
    @Autowired ConsumoSuplementoService consumos;
    @Autowired HabitoService habitoService;
    @Autowired EntityManager entityManager;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired EvaluacionConsumoRepository evaluaciones;
    @Autowired SnapshotEvaluacionConsumoRepository snapshotsConsumo;
    private final UsernamePasswordAuthenticationToken admin =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Test
    @Transactional
    void tresHabitualesPeroSoloUnoConsumidoYCantidadRealIndependiente() {
        var cliente = cliente("uno-de-tres");
        var asignados =
                List.of(
                        asignar(cliente.getId(), "Creatina"),
                        asignar(cliente.getId(), "Multivitamínico"),
                        asignar(cliente.getId(), "Proteína"));
        var h = habito(cliente, LocalDate.of(2026, 8, 24));
        assertThat(suplementos.habituales(cliente.getId(), h.getFecha(), admin)).hasSize(3);
        assertThat(consumos.listar(h.getId(), admin)).isEmpty();
        var diario =
                consumos.crear(
                        h.getId(),
                        new RegistroConsumoSuplementoRequest(
                                asignados.getFirst(),
                                new BigDecimal("3"),
                                "G",
                                1,
                                "Cantidad real del día"),
                        admin);
        entityManager.flush();
        entityManager.clear();
        assertThat(consumos.listar(h.getId(), admin))
                .singleElement()
                .satisfies(
                        c -> {
                            assertThat(c.suplementoClienteId()).isEqualTo(asignados.getFirst());
                            assertThat(c.cantidadConsumida()).isEqualByComparingTo("3");
                        });
        assertThat(
                        habituales
                                .findById(diario.suplementoClienteId())
                                .orElseThrow()
                                .getCantidadPorToma())
                .isEqualByComparingTo("5");
        assertThat(habitos.findById(h.getId()).orElseThrow().getConsumeSuplementos()).isTrue();
    }

    @Test
    @Transactional
    void multiplesConsumosPersistenEditanYNoSePierdenAlEditarHabito() {
        var cliente = cliente("multiples");
        Long uno = asignar(cliente.getId(), "Creatina");
        Long dos = asignar(cliente.getId(), "Vitamina");
        var h = habito(cliente, LocalDate.of(2026, 8, 23));
        var c1 =
                consumos.crear(
                        h.getId(),
                        new RegistroConsumoSuplementoRequest(
                                uno, new BigDecimal("5"), "G", 1, null),
                        admin);
        var c2 =
                consumos.crear(
                        h.getId(),
                        new RegistroConsumoSuplementoRequest(
                                dos, BigDecimal.ONE, "CAPSULA", 1, null),
                        admin);
        consumos.actualizar(
                h.getId(),
                c1.id(),
                new RegistroConsumoSuplementoRequest(uno, new BigDecimal("2.5"), "G", 1, "editado"),
                admin);
        habitoService.update(
                h.getId(),
                new HabitoUpdateRequest(
                        h.getFecha(),
                        4,
                        2.5,
                        90.0,
                        "OMNIVORA",
                        "ALTA",
                        true,
                        false,
                        null,
                        3,
                        null,
                        true),
                admin);
        entityManager.flush();
        entityManager.clear();
        assertThat(consumos.listar(h.getId(), admin))
                .hasSize(2)
                .anySatisfy(
                        c -> {
                            assertThat(c.id()).isEqualTo(c1.id());
                            assertThat(c.cantidadConsumida()).isEqualByComparingTo("2.5");
                        });
        consumos.eliminar(h.getId(), c1.id(), admin);
        assertThat(habitos.findById(h.getId()).orElseThrow().getConsumeSuplementos()).isTrue();
        consumos.eliminar(h.getId(), c2.id(), admin);
        assertThat(consumos.listar(h.getId(), admin)).isEmpty();
        assertThat(habitos.findById(h.getId()).orElseThrow().getConsumeSuplementos()).isFalse();
    }

    @Test
    @Transactional
    void editarHabitoNoPuedeContradecirConsumosEstructurados() {
        var cliente = cliente("boolean-derivado");
        Long habitual = asignar(cliente.getId(), "Creatina");
        var h = habito(cliente, LocalDate.of(2026, 8, 22));
        consumos.crear(
                h.getId(),
                new RegistroConsumoSuplementoRequest(habitual, new BigDecimal("3"), "G", 1, null),
                admin);
        habitoService.update(
                h.getId(),
                new HabitoUpdateRequest(
                        h.getFecha(),
                        3,
                        2.0,
                        80.0,
                        "OMNIVORA",
                        "MEDIA",
                        true,
                        false,
                        null,
                        2,
                        null,
                        false),
                admin);
        assertThat(habitos.findById(h.getId()).orElseThrow().getConsumeSuplementos()).isTrue();
    }

    @Test
    @Transactional
    void evaluacionFactualFechaCorteExcluyeFuturoYAltoConsumoNull() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "pcs-" + UUID.randomUUID() + "@test.local", "Password1!", "PCS"));
        var cliente = clientes.findById(reg.clienteId()).orElseThrow();
        Long habitualId = asignar(cliente.getId(), "Creatina");
        LocalDate corte = LocalDate.of(2026, 8, 20);
        int ventana = 7;
        LocalDate inicio = corte.minusDays(ventana - 1);
        var hDentro = habito(cliente, corte.minusDays(2));
        var hFuturo = habito(cliente, corte.plusDays(1));
        consumos.crear(
                hDentro.getId(),
                new RegistroConsumoSuplementoRequest(habitualId, new BigDecimal("3"), "G", 1, null),
                admin);
        consumos.crear(
                hFuturo.getId(),
                new RegistroConsumoSuplementoRequest(habitualId, new BigDecimal("9"), "G", 1, null),
                admin);
        entityManager.flush();
        entityManager.clear();
        String body =
                "{\"clienteId\":%d,\"fechaCorte\":\"%s\",\"ventanaDias\":%d,\"momento\":\"BASAL\"}"
                        .formatted(cliente.getId(), corte, ventana);
        MvcResult result =
                mockMvc.perform(
                                post("/api/evaluaciones-consumo")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("fechaInicio").asText()).isEqualTo(inicio.toString());
        assertThat(json.get("fechaCorte").asText()).isEqualTo(corte.toString());
        assertThat(json.get("momento").asText()).isEqualTo("BASAL");
        assertThat(json.get("estadoValidez").asText()).isEqualTo("NO_DETERMINADA");
        assertThat(json.get("altoConsumo").isNull()).isTrue();
        assertThat(json.get("advertencias").isNull()).isTrue();
        Long evaluacionId = json.get("id").asLong();
        var snapshot = snapshotsConsumo.findByEvaluacionId(evaluacionId).orElseThrow();
        assertThat(snapshot.getSchemaVersion()).isEqualTo("pcs-factual-v1");
        JsonNode contenido = objectMapper.readTree(snapshot.getContenidoJson());
        JsonNode consumosReales = contenido.get("consumosReales");
        assertThat(consumosReales.size()).isEqualTo(1);
        assertThat(consumosReales.get(0).get("fecha").asText())
                .isEqualTo(corte.minusDays(2).toString());
        assertThat(contenido.toString()).doesNotContain(corte.plusDays(1).toString());
        assertThat(contenido.get("habitualesContexto").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Transactional
    void snapshotConsumoEsInmutableAnteCambioDeCatalogo() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "inmutable-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Inmutable"));
        var cliente = clientes.findById(reg.clienteId()).orElseThrow();
        var cat =
                suplementos.saveCatalog(
                        null,
                        new SuplementoCatalogoRequest(
                                "CreatinaOriginal " + UUID.randomUUID(),
                                "GENERAL",
                                null,
                                null,
                                null,
                                "Marca",
                                "Presentación",
                                "G",
                                true));
        var asignacion =
                suplementos.assign(
                        cliente.getId(),
                        new SuplementoAsignacionRequest(
                                cat.id(),
                                5.0,
                                "g",
                                "diaria",
                                "habitual",
                                true,
                                LocalDate.of(2026, 8, 1),
                                null,
                                new BigDecimal("5"),
                                "G",
                                1,
                                PeriodoFrecuencia.DIA),
                        admin);
        LocalDate corte = LocalDate.of(2026, 8, 20);
        var h = habito(cliente, corte.minusDays(1));
        consumos.crear(
                h.getId(),
                new RegistroConsumoSuplementoRequest(
                        asignacion.id(), new BigDecimal("3"), "G", 1, null),
                admin);
        entityManager.flush();
        entityManager.clear();
        String body =
                "{\"clienteId\":%d,\"fechaCorte\":\"%s\",\"ventanaDias\":%d}"
                        .formatted(cliente.getId(), corte, 7);
        MvcResult result =
                mockMvc.perform(
                                post("/api/evaluaciones-consumo")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        Long evaluacionId = json.get("id").asLong();
        var snapshot = snapshotsConsumo.findByEvaluacionId(evaluacionId).orElseThrow();
        String jsonOriginal = snapshot.getContenidoJson();
        assertThat(jsonOriginal).contains("CreatinaOriginal");
        suplementos.saveCatalog(
                cat.id(),
                new SuplementoCatalogoRequest(
                        "NombreModificado " + UUID.randomUUID(),
                        "GENERAL",
                        null,
                        null,
                        null,
                        "Marca",
                        "Presentación",
                        "G",
                        true));
        entityManager.flush();
        entityManager.clear();
        var snapshotRecargado = snapshotsConsumo.findByEvaluacionId(evaluacionId).orElseThrow();
        assertThat(snapshotRecargado.getContenidoJson()).isEqualTo(jsonOriginal);
        assertThat(snapshotRecargado.getContenidoJson()).contains("CreatinaOriginal");
        assertThat(snapshotRecargado.getContenidoJson()).doesNotContain("NombreModificado");
    }

    private com.backend.nutri_predic.cliente.entity.Cliente cliente(String p) {
        var r =
                auth.register(
                        new RegisterRequest(
                                p + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Cliente"));
        return clientes.findById(r.clienteId()).orElseThrow();
    }

    private Long asignar(Long clienteId, String nombre) {
        var c =
                suplementos.saveCatalog(
                        null,
                        new SuplementoCatalogoRequest(
                                nombre + " " + UUID.randomUUID(),
                                "GENERAL",
                                null,
                                null,
                                null,
                                "Marca",
                                "Presentación",
                                "G",
                                true));
        return suplementos
                .assign(
                        clienteId,
                        new SuplementoAsignacionRequest(
                                c.id(),
                                5.0,
                                "g",
                                "diaria",
                                "habitual",
                                true,
                                LocalDate.of(2026, 8, 1),
                                null,
                                new BigDecimal("5"),
                                "G",
                                1,
                                PeriodoFrecuencia.DIA),
                        admin)
                .id();
    }

    private RegistroHabito habito(com.backend.nutri_predic.cliente.entity.Cliente c, LocalDate f) {
        var h = new RegistroHabito();
        h.setCliente(c);
        h.setFecha(f);
        h.setCantidadComidas(3);
        h.setConsumoAgua(2.0);
        h.setProteinas(80.0);
        h.setTipoAlimentacion("OMNIVORA");
        h.setNivelOrganizacion("MEDIA");
        h.setDesayuno(true);
        h.setSnacks(false);
        h.setComidasCocinadas(2);
        h.setConsumeSuplementos(false);
        return habitos.save(h);
    }
}
