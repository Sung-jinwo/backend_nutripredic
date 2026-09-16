package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoRequest;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoResponse;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import com.backend.nutri_predic.consumo.repository.RubricaConsumoSuplementosRepository;
import com.backend.nutri_predic.consumo.repository.SnapshotEvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.service.ConsumoEvaluacionExtractor;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.suplemento.dto.RegistroConsumoSuplementoRequest;
import com.backend.nutri_predic.suplemento.dto.SuplementoAsignacionRequest;
import com.backend.nutri_predic.suplemento.dto.SuplementoCatalogoRequest;
import com.backend.nutri_predic.suplemento.entity.PeriodoFrecuencia;
import com.backend.nutri_predic.suplemento.service.ConsumoSuplementoService;
import com.backend.nutri_predic.suplemento.service.SuplementoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class PcsRubricaEvaluacionIntegrationTests {
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);
    private final UsernamePasswordAuthenticationToken admin =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired SuplementoService suplementos;
    @Autowired ConsumoSuplementoService consumos;
    @Autowired ConsumoEvaluacionExtractor evaluador;
    @Autowired RubricaConsumoSuplementosRepository rubricas;
    @Autowired SnapshotEvaluacionConsumoRepository snapshots;
    @Autowired ObjectMapper json;
    @Autowired com.backend.nutri_predic.consumo.repository.CriterioConsumoSuplementosRepository criterios;

    @Test
    @Transactional
    void rubricaTecnicaVaciaNoDesplazaReferenciaConCriterios() {
        var oficial = rubricas.saveAndFlush(rubrica(EstadoCriterioConsumo.ACTIVO, true));
        var regla = new com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos();
        regla.setRubrica(oficial);
        regla.setAlcance("COMPONENTE");
        regla.setComponenteTipo("CAFEINA");
        regla.setCantidadReferencia(new BigDecimal("400"));
        regla.setTipoEvaluador("MAYOR_QUE");
        criterios.saveAndFlush(regla);
        var vacia = rubrica(EstadoCriterioConsumo.ACTIVO, true);
        vacia.setVersion(999);
        var guardada = rubricas.saveAndFlush(vacia);
        var candidatas = rubricas.findConCriteriosOrderByVersionDescIdDesc();
        assertThat(candidatas).noneMatch(r -> r.getId().equals(guardada.getId()));
        assertThat(candidatas).anyMatch(r -> r.getId().equals(oficial.getId()));
    }

    @Test
    @Transactional
    void sinRubricaQuedaNoDeterminada() {
        assertNoDeterminada(evaluar(cliente("sin-rubrica")));
    }

    @Test
    @Transactional
    void rubricaInactivaQuedaNoDeterminada() {
        rubricas.save(rubrica(EstadoCriterioConsumo.INACTIVO, true));
        var respuesta = evaluar(cliente("rubrica-inactiva"));
        assertNoDeterminada(respuesta);
        assertThat(respuesta.rubricaId()).isNull();
    }

    @Test
    @Transactional
    void rubricaNoValidadaQuedaNoDeterminada() {
        rubricas.save(rubrica(EstadoCriterioConsumo.ACTIVO, false));
        var respuesta = evaluar(cliente("rubrica-no-validada"));
        assertNoDeterminada(respuesta);
        assertThat(respuesta.rubricaId()).isNull();
    }

    @Test
    @Transactional
    void snapshotFactualRespetaFechaCorte() throws Exception {
        var cliente = cliente("fecha-corte");
        Long asignacion = asignar(cliente.getId());
        consumo(cliente, asignacion, CORTE.minusDays(2), "3");
        consumo(cliente, asignacion, CORTE.plusDays(1), "9");

        var evaluacion = evaluar(cliente);
        var contenido =
                json.readTree(
                        snapshots
                                .findByEvaluacionId(evaluacion.id())
                                .orElseThrow()
                                .getContenidoJson());

        assertThat(contenido.get("consumosReales")).hasSize(1);
        assertThat(contenido.get("consumosReales").get(0).get("fecha").asText())
                .isEqualTo(CORTE.minusDays(2).toString());
        assertThat(contenido.toString()).doesNotContain(CORTE.plusDays(1).toString());
    }

    @Test
    @Transactional
    void datosPosterioresNoAfectanEvaluacionHistorica() {
        var cliente = cliente("historica");
        Long asignacion = asignar(cliente.getId());
        consumo(cliente, asignacion, CORTE.minusDays(1), "3");
        var evaluacion = evaluar(cliente);
        String snapshotOriginal =
                snapshots.findByEvaluacionId(evaluacion.id()).orElseThrow().getContenidoJson();

        consumo(cliente, asignacion, CORTE.plusDays(1), "12");

        assertThat(snapshots.findByEvaluacionId(evaluacion.id()).orElseThrow().getContenidoJson())
                .isEqualTo(snapshotOriginal)
                .doesNotContain(CORTE.plusDays(1).toString());
    }

    private EvaluacionConsumoResponse evaluar(Cliente cliente) {
        return evaluador.extraer(new EvaluacionConsumoRequest(cliente.getId(), CORTE, 7, null));
    }

    private void assertNoDeterminada(EvaluacionConsumoResponse respuesta) {
        assertThat(respuesta.altoConsumo()).isNull();
        assertThat(respuesta.estadoClasificacion()).isEqualTo("NO_DETERMINADA");
        assertThat(respuesta.motivo()).isEqualTo("CRITERIO_NO_CONFIGURADO");
    }

    private RubricaConsumoSuplementos rubrica(EstadoCriterioConsumo estado, boolean validada) {
        var rubrica = new RubricaConsumoSuplementos();
        rubrica.setCodigo("PCS-" + UUID.randomUUID());
        rubrica.setVersion(1);
        rubrica.setEstado(estado);
        rubrica.setValidada(validada);
        rubrica.setVigenteDesde(CORTE.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        rubrica.setVigenteHasta(CORTE.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        rubrica.setObservacion("Fixture estructural sin umbrales");
        return rubrica;
    }

    private Cliente cliente(String prefijo) {
        var registro =
                auth.register(
                        new RegisterRequest(
                                prefijo + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "PCS"));
        return clientes.findById(registro.clienteId()).orElseThrow();
    }

    private Long asignar(Long clienteId) {
        var catalogo =
                suplementos.saveCatalog(
                        null,
                        new SuplementoCatalogoRequest(
                                "PCS factual " + UUID.randomUUID(),
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
                                catalogo.id(),
                                5.0,
                                "g",
                                "diaria",
                                "habitual",
                                true,
                                CORTE.minusDays(20),
                                null,
                                new BigDecimal("5"),
                                "G",
                                1,
                                PeriodoFrecuencia.DIA),
                        admin)
                .id();
    }

    private void consumo(Cliente cliente, Long asignacion, LocalDate fecha, String cantidad) {
        var habito = new RegistroHabito();
        habito.setCliente(cliente);
        habito.setFecha(fecha);
        habito.setCantidadComidas(3);
        habito.setConsumoAgua(2.0);
        habito.setProteinas(80.0);
        habito.setTipoAlimentacion("OMNIVORA");
        habito.setNivelOrganizacion("MEDIA");
        habito.setDesayuno(true);
        habito.setSnacks(false);
        habito.setComidasCocinadas(2);
        habito.setConsumeSuplementos(false);
        habito = habitos.save(habito);
        consumos.crear(
                habito.getId(),
                new RegistroConsumoSuplementoRequest(
                        asignacion, new BigDecimal(cantidad), "G", 1, null),
                admin);
    }
}
