package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.*;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.alimentacion.service.AlimentacionService;
import com.backend.nutri_predic.alimentacion.service.ComposicionNutricionalAlimentoService;
import com.backend.nutri_predic.alimentacion.service.EquivalenciaUnidadAlimentoService;
import com.backend.nutri_predic.alimentacion.service.NutricionAlimentoService;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.dto.ClienteRequest;
import com.backend.nutri_predic.cliente.repository.*;
import com.backend.nutri_predic.cliente.service.ClienteService;
import com.backend.nutri_predic.cliente.service.PerfilClienteHistoricoService;
import com.backend.nutri_predic.common.enums.EstadoCliente;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.conocimiento.dto.PreguntaRequest;
import com.backend.nutri_predic.conocimiento.repository.PreguntaConocimientoRepository;
import com.backend.nutri_predic.conocimiento.service.ConocimientoService;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.entity.PeriodoFrecuencia;
import com.backend.nutri_predic.suplemento.repository.*;
import com.backend.nutri_predic.suplemento.service.SuplementoService;
import com.backend.nutri_predic.conocimiento.evaluacion.dto.ResponderTestRequest;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.*;
import com.backend.nutri_predic.conocimiento.evaluacion.service.TestService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class Phase34DataStructureIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ConocimientoService conocimiento;
    @Autowired PreguntaConocimientoRepository preguntas;
    @Autowired TestService tests;
    @Autowired ResultadoTemaTestRepository temas;
    @Autowired RespuestaTestRepository respuestas;
    @Autowired SuplementoService suplementos;
    @Autowired SuplementoCatalogoRepository catalogosSuplemento;
    @Autowired SuplementoClienteRepository asignaciones;
    @Autowired HistorialSuplementoClienteRepository historialSuplementos;
    @Autowired AlimentacionService alimentacion;
    @Autowired ComposicionNutricionalAlimentoService composicionesAlimento;
    @Autowired EquivalenciaUnidadAlimentoService equivalenciasAlimento;
    @Autowired NutricionAlimentoService nutricionAlimento;
    @Autowired RegistroAlimentoRepository registrosAlimentos;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired ClienteRepository clientes;
    @Autowired ClienteService clienteService;
    @Autowired HistorialPerfilClienteRepository historialPerfil;
    @Autowired PerfilClienteHistoricoService perfiles;
    private final UsernamePasswordAuthenticationToken admin =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Test
    @Transactional
    void preguntaVersionadaConservaEvaluacionYResultadoPorTema() {
        var cliente = cliente("pregunta");
        var v1 = conocimiento.create(pregunta("¿Agua?", "HIDRATACION", "A"));
        var resultado =
                tests.submit(
                        new ResponderTestRequest(
                                cliente.getId(),
                                List.of(new ResponderTestRequest.Respuesta(v1.id(), "A"))),
                        admin);
        var v2 = conocimiento.createVersion(v1.id(), pregunta("¿Agua diaria?", "HIDRATACION", "B"));
        var historico = tests.get(cliente.getId(), resultado.id(), admin);
        assertThat(v2.version()).isEqualTo(2);
        assertThat(v2.grupoVersion()).isEqualTo(v1.grupoVersion());
        assertThat(preguntas.findById(v1.id()).orElseThrow().getEnunciado()).isEqualTo("¿Agua?");
        assertThat(historico.respuestas())
                .singleElement()
                .satisfies(
                        r -> {
                            assertThat(r.version()).isEqualTo(1);
                            assertThat(r.texto()).isEqualTo("¿Agua?");
                            assertThat(r.respuestaCorrecta()).isEqualTo("A");
                        });
        assertThat(historico.resultadosPorTema())
                .singleElement()
                .satisfies(
                        t -> {
                            assertThat(t.tema()).isEqualTo("HIDRATACION");
                            assertThat(t.correctas()).isEqualTo(1);
                            assertThat(t.porcentaje()).isEqualTo(100.0);
                        });
    }

    @Test
    @Transactional
    void suplementoMantieneLegacyYAgregaComposicionFrecuenciaControlada() {
        var cliente = cliente("suplemento");
        var cat =
                suplementos.saveCatalog(
                        null,
                        new SuplementoCatalogoRequest(
                                "Creatina " + UUID.randomUUID(),
                                "DEPORTIVO",
                                "Descripción legacy",
                                null,
                                null,
                                "Marca X",
                                "500 g",
                                "G",
                                true));
        var asignada =
                suplementos.assign(
                        cliente.getId(),
                        new SuplementoAsignacionRequest(
                                cat.id(),
                                5.0,
                                "g",
                                "diaria",
                                "8 semanas",
                                true,
                                LocalDate.of(2026, 8, 1),
                                null,
                                new BigDecimal("5"),
                                "G",
                                1,
                                PeriodoFrecuencia.DIA),
                        admin);
        assertThat(asignada.frecuenciaEstructuradaCompleta()).isTrue();
        assertThat(asignada.frecuencia()).isEqualTo("diaria");
        assertThat(catalogosSuplemento.findById(cat.id())).isPresent();
        assertThat(asignaciones.findByClienteId(cliente.getId())).hasSize(1);
    }

    @Test
    @Transactional
    void actualizarYRetirarSuplementoConservaVersiones() {
        var cliente = cliente("historial-suplemento");
        var cat =
                suplementos.saveCatalog(
                        null,
                        new SuplementoCatalogoRequest(
                                "Proteína " + UUID.randomUUID(),
                                "PROTEINA",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true));
        suplementos.assign(
                cliente.getId(),
                new SuplementoAsignacionRequest(
                        cat.id(),
                        20.0,
                        "g",
                        "diaria",
                        "1 mes",
                        true,
                        LocalDate.of(2026, 8, 1),
                        null,
                        new BigDecimal("20"),
                        "G",
                        1,
                        PeriodoFrecuencia.DIA),
                admin);
        suplementos.update(
                cliente.getId(),
                cat.id(),
                new SuplementoActualizacionRequest(
                        25.0,
                        "g",
                        "diaria",
                        "2 meses",
                        true,
                        LocalDate.of(2026, 8, 1),
                        null,
                        new BigDecimal("25"),
                        "G",
                        1,
                        PeriodoFrecuencia.DIA),
                admin);
        var asignacion =
                asignaciones
                        .findByClienteIdAndSuplementoId(cliente.getId(), cat.id())
                        .orElseThrow();
        suplementos.remove(cliente.getId(), cat.id(), admin);
        assertThat(
                        historialSuplementos.findByAsignacionIdOrderByRegistradoEnAscIdAsc(
                                asignacion.getId()))
                .hasSize(3)
                .extracting(h -> h.getCantidadPorToma())
                .containsExactly(new BigDecimal("20"), new BigDecimal("25"), new BigDecimal("25"));
        assertThat(asignaciones.findById(asignacion.getId()))
                .get()
                .extracting(s -> s.getActivo())
                .isEqualTo(false);
    }

    @Test
    @Transactional
    void alimentosSonReutilizablesRecientesFrecuentesYPlantillaNoCopiaHistorial() {
        var cliente = cliente("alimentos");
        var arroz =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Arroz " + UUID.randomUUID(), "CEREAL", "G", true));
        var pollo =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Pollo " + UUID.randomUUID(), "PROTEINA", "G", true));
        var h1 = habito(cliente, LocalDate.of(2026, 8, 20));
        var h2 = habito(cliente, LocalDate.of(2026, 8, 21));
        alimentacion.agregarRegistro(
                h1.getId(),
                new RegistroAlimentoRequest(
                        arroz.id(), new BigDecimal("120"), "G", MomentoComida.ALMUERZO),
                admin);
        alimentacion.agregarRegistro(
                h2.getId(),
                new RegistroAlimentoRequest(
                        arroz.id(), new BigDecimal("150"), "G", MomentoComida.ALMUERZO),
                admin);
        alimentacion.agregarRegistro(
                h2.getId(),
                new RegistroAlimentoRequest(
                        pollo.id(), new BigDecimal("180"), "G", MomentoComida.ALMUERZO),
                admin);
        assertThat(alimentacion.recientes(cliente.getId(), admin))
                .extracting(AlimentoUsoResponse::alimentoId)
                .containsExactly(pollo.id(), arroz.id());
        assertThat(alimentacion.frecuentes(cliente.getId(), admin).getFirst().alimentoId())
                .isEqualTo(arroz.id());
        assertThat(alimentacion.frecuentes(cliente.getId(), admin).getFirst().ultimaCantidad())
                .isEqualByComparingTo("150");
    }

    @Test
    @Transactional
    void composicionVersionadaSeAsociaSoloANuevosRegistrosYCalculaSinConversiones() {
        var cliente = cliente("composicion");
        var alimento =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Avena " + UUID.randomUUID(), "CEREAL", "G", true));
        var legacy = habito(cliente, LocalDate.of(2026, 8, 19));
        alimentacion.agregarRegistro(
                legacy.getId(),
                new RegistroAlimentoRequest(
                        alimento.id(), new BigDecimal("50"), "G", MomentoComida.DESAYUNO),
                admin);
        var v1 =
                composicionesAlimento.crear(
                        alimento.id(),
                        new ComposicionNutricionalAlimentoRequest(
                                new BigDecimal("100"),
                                "G",
                                new BigDecimal("389"),
                                new BigDecimal("16.9"),
                                new BigDecimal("66"),
                                new BigDecimal("6.9"),
                                BigDecimal.ZERO,
                                null,
                                null,
                                "Fuente",
                                LocalDate.of(2026, 8, 20),
                                null,
                                true));
        var actual = habito(cliente, LocalDate.of(2026, 8, 20));
        alimentacion.agregarRegistro(
                actual.getId(),
                new RegistroAlimentoRequest(
                        alimento.id(), new BigDecimal("50"), "G", MomentoComida.DESAYUNO),
                admin);
        var legacyEntidad =
                registrosAlimentos.findByRegistroHabitoIdOrderByIdAsc(legacy.getId()).getFirst();
        var entidad =
                registrosAlimentos.findByRegistroHabitoIdOrderByIdAsc(actual.getId()).getFirst();
        assertThat(legacyEntidad.getComposicionNutricional()).isNull();
        assertThat(nutricionAlimento.calcular(legacyEntidad).calculable()).isFalse();
        assertThat(entidad.getComposicionNutricional().getId()).isEqualTo(v1.id());
        var aporte = nutricionAlimento.calcular(entidad);
        assertThat(aporte.calculable()).isTrue();
        assertThat(aporte.proteinaG()).isEqualByComparingTo("8.45");
        assertThat(aporte.fibraG()).isZero();
        assertThat(aporte.azucarG()).isNull();
        alimentacion.agregarRegistro(
                actual.getId(),
                new RegistroAlimentoRequest(
                        alimento.id(), new BigDecimal("250"), "ML", MomentoComida.DESAYUNO),
                admin);
        assertThat(
                        nutricionAlimento
                                .calcular(
                                        registrosAlimentos
                                                .findByRegistroHabitoIdOrderByIdAsc(actual.getId())
                                                .get(1))
                                .calculable())
                .isFalse();
        var leche =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Leche " + UUID.randomUUID(), "LACTEO", "ML", true));
        composicionesAlimento.crear(
                leche.id(),
                new ComposicionNutricionalAlimentoRequest(
                        new BigDecimal("100"),
                        "ML",
                        new BigDecimal("60"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Fuente",
                        LocalDate.of(2026, 8, 20),
                        null,
                        true));
        alimentacion.agregarRegistro(
                actual.getId(),
                new RegistroAlimentoRequest(
                        leche.id(), new BigDecimal("250"), "ML", MomentoComida.DESAYUNO),
                admin);
        var aporteMl =
                nutricionAlimento.calcular(
                        registrosAlimentos
                                .findByRegistroHabitoIdOrderByIdAsc(actual.getId())
                                .get(2));
        assertThat(aporteMl.calculable()).isTrue();
        assertThat(aporteMl.kcal()).isEqualByComparingTo("150");
        var v2 =
                composicionesAlimento.crear(
                        alimento.id(),
                        new ComposicionNutricionalAlimentoRequest(
                                new BigDecimal("100"),
                                "G",
                                new BigDecimal("400"),
                                new BigDecimal("20"),
                                null,
                                null,
                                BigDecimal.ZERO,
                                null,
                                null,
                                "Fuente 2",
                                LocalDate.of(2026, 8, 21),
                                null,
                                true));
        assertThat(composicionesAlimento.listar(alimento.id()))
                .extracting(ComposicionNutricionalAlimentoResponse::version)
                .containsExactly(2, 1);
        assertThat(v1.version()).isEqualTo(1);
        assertThat(v2.version()).isEqualTo(2);
    }

    @Test
    @Transactional
    void equivalenciaSeguraConvierteSoloSiEsUnicaVigenteYActualizarConservaRegistro() {
        var cliente = cliente("equivalencia");
        var alimento =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Harina " + UUID.randomUUID(), "CEREAL", "G", true));
        var fecha = LocalDate.of(2026, 8, 22);
        composicionesAlimento.crear(
                alimento.id(),
                new ComposicionNutricionalAlimentoRequest(
                        new BigDecimal("100"),
                        "G",
                        new BigDecimal("400"),
                        new BigDecimal("10"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Fuente",
                        fecha,
                        null,
                        true));
        var eq =
                equivalenciasAlimento.crear(
                        alimento.id(),
                        new EquivalenciaUnidadAlimentoRequest(
                                BigDecimal.ONE,
                                "UNIDAD",
                                new BigDecimal("50"),
                                "G",
                                "FIXTURE TECNICO",
                                fecha,
                                null,
                                true));
        assertThat(equivalenciasAlimento.listarActivas(alimento.id(), fecha))
                .extracting(EquivalenciaUnidadAlimentoResponse::id)
                .containsExactly(eq.id());
        var h = habito(cliente, fecha);
        var creado =
                alimentacion.agregarRegistro(
                        h.getId(),
                        new RegistroAlimentoRequest(
                                alimento.id(),
                                new BigDecimal("2"),
                                "UNIDAD",
                                MomentoComida.MERIENDA),
                        admin);
        var registro = registrosAlimentos.findById(creado.id()).orElseThrow();
        assertThat(registro.getEquivalenciaUnidad().getId()).isEqualTo(eq.id());
        assertThat(nutricionAlimento.calcular(registro).proteinaG()).isEqualByComparingTo("10");
        var actualizado =
                alimentacion.actualizarRegistro(
                        h.getId(),
                        creado.id(),
                        new RegistroAlimentoRequest(
                                alimento.id(), new BigDecimal("50"), "G", MomentoComida.MERIENDA),
                        admin);
        assertThat(actualizado.id()).isEqualTo(creado.id());
        var directo = registrosAlimentos.findById(creado.id()).orElseThrow();
        assertThat(directo.getEquivalenciaUnidad()).isNull();
        assertThat(nutricionAlimento.calcular(directo).proteinaG()).isEqualByComparingTo("5");
        equivalenciasAlimento.crear(
                alimento.id(),
                new EquivalenciaUnidadAlimentoRequest(
                        BigDecimal.ONE,
                        "UNIDAD",
                        new BigDecimal("60"),
                        "G",
                        "FIXTURE AMBIGUA",
                        fecha,
                        null,
                        true));
        alimentacion.agregarRegistro(
                h.getId(),
                new RegistroAlimentoRequest(
                        alimento.id(), BigDecimal.ONE, "UNIDAD", MomentoComida.CENA),
                admin);
        var ambiguo = registrosAlimentos.findByRegistroHabitoIdOrderByIdAsc(h.getId()).getLast();
        assertThat(ambiguo.getEquivalenciaUnidad()).isNull();
        assertThat(nutricionAlimento.calcular(ambiguo).calculable()).isFalse();
    }

    @Test
    @Transactional
    void perfilSoloVersionaCambiosYResolutorUsaHistorialAplicable() {
        var c = cliente("perfil");
        var base =
                new ClienteRequest(
                        30,
                        new BigDecimal("70"),
                        new BigDecimal("170"),
                        "Mantener",
                        null,
                        EstadoCliente.ACTIVO);
        clienteService.update(c.getId(), base, admin);
        clienteService.update(c.getId(), base, admin);
        clienteService.update(
                c.getId(),
                new ClienteRequest(
                        30,
                        new BigDecimal("72"),
                        new BigDecimal("170"),
                        "Mantener",
                        null,
                        EstadoCliente.ACTIVO),
                admin);
        assertThat(
                        historialPerfil.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(
                                c.getId()))
                .hasSize(2)
                .extracting(h -> h.getPesoKg())
                .contains(new BigDecimal("70"), new BigDecimal("72"));
        assertThat(clientes.findById(c.getId()).orElseThrow().getPesoKg())
                .isEqualByComparingTo("72");
        // El perfil aplicable se resuelve desde el historial (fuente de V5/V6),
        // no desde un extractor legacy: la versión con peso 72 debe aplicar.
        var aplicable = perfiles.resolver(c.getId(), LocalDate.now());
        assertThat(aplicable.fuentePerfil()).isEqualTo("HISTORIAL");
        assertThat(aplicable.pesoKg()).isEqualByComparingTo("72");
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

    private PreguntaRequest pregunta(String texto, String tema, String correcta) {
        return new PreguntaRequest(
                texto, tema, null, "BASICA", "A", "B", "C", "D", correcta, "Explicación", "Fuente");
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
