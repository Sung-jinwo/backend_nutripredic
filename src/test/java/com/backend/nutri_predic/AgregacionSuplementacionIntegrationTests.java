package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.auth.dto.LoginRequest;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.Rol;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.suplemento.dto.ComponenteComposicionSuplementoRequest;
import com.backend.nutri_predic.suplemento.dto.ComposicionSuplementoRequest;
import com.backend.nutri_predic.suplemento.dto.EquivalenciaUnidadSuplementoRequest;
import com.backend.nutri_predic.suplemento.dto.RegistroConsumoSuplementoRequest;
import com.backend.nutri_predic.suplemento.dto.ResumenSuplementacionResponse;
import com.backend.nutri_predic.suplemento.dto.SuplementoAsignacionRequest;
import com.backend.nutri_predic.suplemento.dto.SuplementoCatalogoRequest;
import com.backend.nutri_predic.suplemento.entity.PeriodoFrecuencia;
import com.backend.nutri_predic.suplemento.entity.SuplementoCliente;
import com.backend.nutri_predic.suplemento.entity.TipoComponenteSuplemento;
import com.backend.nutri_predic.suplemento.repository.SuplementoClienteRepository;
import com.backend.nutri_predic.suplemento.service.AgregacionSuplementacionService;
import com.backend.nutri_predic.suplemento.service.ComposicionSuplementoService;
import com.backend.nutri_predic.suplemento.service.ConsumoSuplementoService;
import com.backend.nutri_predic.suplemento.service.EquivalenciaUnidadSuplementoService;
import com.backend.nutri_predic.suplemento.service.SuplementoClienteHistoricoService;
import com.backend.nutri_predic.suplemento.service.SuplementoService;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AgregacionSuplementacionIntegrationTests {
    private static final LocalDate CORTE = LocalDate.now(java.time.ZoneOffset.UTC);
    private static final UsernamePasswordAuthenticationToken ADMIN =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired SuplementoService suplementos;
    @Autowired SuplementoClienteRepository suplementosCliente;
    @Autowired ComposicionSuplementoService composiciones;
    @Autowired EquivalenciaUnidadSuplementoService equivalencias;
    @Autowired ConsumoSuplementoService consumos;
    @Autowired AgregacionSuplementacionService agregacion;
    @Autowired SuplementoClienteHistoricoService historico;
    @Autowired UnidadMedidaRepository unidades;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired MockMvc mockMvc;

    @Test
    @Transactional
    void agregaCantidadPorTomaConNumeroDeTomasEnVentanaInclusiva() {
        Cliente cliente = cliente("agregacion");
        Long proteina48 = suplementoConProteina(cliente, "Proteina 48", "24", true);
        Long proteina22 = suplementoConProteina(cliente, "Proteina 22", "22", false);
        Long sinEquivalencia = suplementoConProteina(cliente, "No calculable", "10", false);
        Long habitualSinConsumo = suplementoConProteina(cliente, "Solo habitual", "5", false);

        RegistroHabito inicio = habito(cliente, CORTE.minusDays(6));
        RegistroHabito finalVentana = habito(cliente, CORTE);
        RegistroHabito futuro = habito(cliente, CORTE.plusDays(1));
        RegistroHabito anterior = habito(cliente, CORTE.minusDays(7));
        consumos.crear(inicio.getId(), consumo(proteina22, "30", "G", 1), ADMIN);
        consumos.crear(finalVentana.getId(), consumo(proteina48, "60", "G", 2), ADMIN);
        consumos.crear(finalVentana.getId(), consumo(sinEquivalencia, "1", "SCOOP", 1), ADMIN);
        consumos.crear(futuro.getId(), consumo(proteina48, "60", "G", 1), ADMIN);
        consumos.crear(anterior.getId(), consumo(proteina22, "30", "G", 1), ADMIN);

        ResumenSuplementacionResponse respuesta = agregacion.resumir(cliente.getId(), CORTE);
        var ventana = respuesta.resumenVentana();
        var diaSinConsumo =
                respuesta.resumenDiario().stream()
                        .filter(dia -> dia.fecha().equals(CORTE.minusDays(1)))
                        .findFirst()
                        .orElseThrow();

        assertThat(respuesta.resumenDiario()).hasSize(7);
        assertThat(ventana.fechaDesde()).isEqualTo(CORTE.minusDays(6));
        assertThat(ventana.fechaHasta()).isEqualTo(CORTE);
        assertThat(ventana.registrosConsumoTotal()).isEqualTo(3);
        assertThat(ventana.registrosCalculables()).isEqualTo(2);
        assertThat(ventana.registrosNoCalculables()).isEqualTo(1);
        // 60 g por toma / 30 g de referencia * 24 g proteína * 2 tomas + 22 g.
        assertThat(ventana.proteinaSuplementariaGTotal()).isEqualByComparingTo("118");
        assertThat(ventana.promedioProteinaSobreVentana()).isEqualByComparingTo("16.857143");
        assertThat(ventana.promedioProteinaSobreDiasConConsumo()).isEqualByComparingTo("59");
        assertThat(ventana.cafeinaMgTotal()).isNull();
        assertThat(diaSinConsumo.registrosConsumoTotal()).isZero();
        assertThat(diaSinConsumo.proteinaSuplementariaG()).isNull();
        assertThat(diaSinConsumo.cafeinaMg()).isNull();
        assertThat(respuesta.suplementacionHabitual())
                .hasSize(4)
                .allSatisfy(s -> assertThat(s.fuente()).isEqualTo("HISTORIAL"));
        assertThat(
                        respuesta.suplementacionHabitual().stream()
                                .anyMatch(s -> s.suplementoClienteId().equals(habitualSinConsumo)))
                .isTrue();
    }

    @Test
    @Transactional
    void contextoHistoricoUsaFallbackActualCuandoNoHayVersionHistorica() {
        Cliente cliente = cliente("fallback");
        var catalogo = suplementos.saveCatalog(null, catalogo("Fallback"));
        SuplementoCliente actual = new SuplementoCliente();
        actual.setCliente(cliente);
        actual.setNombreDeclarado(catalogo.nombre());
        actual.setSuplemento(new com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo());
        // La asignación se completa con el catálogo persistido para no generar historial.
        actual.setSuplemento(entityManagerReference(catalogo.id()));
        actual.setActivo(true);
        actual.setFechaInicio(CORTE.minusDays(1));
        actual.setCantidadPorToma(BigDecimal.ONE);
        actual.setUnidadMedida(unidades.findByCodigoIgnoreCaseAndActivaTrue("G").orElseThrow());
        actual.setTomasPorPeriodo(1);
        actual.setPeriodoFrecuencia(PeriodoFrecuencia.DIA);
        suplementosCliente.save(actual);

        assertThat(historico.resolver(cliente.getId(), CORTE))
                .singleElement()
                .satisfies(
                        resultado -> assertThat(resultado.fuente()).isEqualTo("FALLBACK_ACTUAL"));
    }

    @Test
    @Transactional
    void endpointAdminExponeResumenSinRelacionesJPA() throws Exception {
        Cliente cliente = cliente("endpoint");
        mockMvc.perform(
                        get(
                                        "/api/admin/clientes/{clienteId}/suplementacion/resumen",
                                        cliente.getId())
                                .queryParam("fechaCorte", CORTE.toString()))
                .andExpect(status().isUnauthorized());
        String email = "admin-" + UUID.randomUUID() + "@test.local";
        usuarios.save(new Usuario(email, passwordEncoder.encode("Password1!"), "Admin", Rol.ADMIN));
        String token = auth.login(new LoginRequest(email, "Password1!")).token();

        mockMvc.perform(
                        get(
                                        "/api/admin/clientes/{clienteId}/suplementacion/resumen",
                                        cliente.getId())
                                .queryParam("fechaCorte", CORTE.toString())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaCorte").value(CORTE.toString()))
                .andExpect(jsonPath("$.resumenDiario").isArray())
                .andExpect(jsonPath("$.resumenDiario.length()").value(7))
                .andExpect(jsonPath("$.resumenVentana.diasVentana").value(7));
    }

    private Cliente cliente(String prefijo) {
        var registro =
                auth.register(
                        new RegisterRequest(
                                prefijo + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Cliente"));
        return clientes.findById(registro.clienteId()).orElseThrow();
    }

    private Long suplementoConProteina(
            Cliente cliente, String nombre, String proteina, boolean conEquivalencia) {
        var catalogo = suplementos.saveCatalog(null, catalogo(nombre));
        var asignacion =
                suplementos.assign(
                        cliente.getId(),
                        new SuplementoAsignacionRequest(
                                catalogo.id(),
                                5.0,
                                "g",
                                "diaria",
                                "habitual",
                                true,
                                CORTE.minusDays(30),
                                null,
                                new BigDecimal("30"),
                                "G",
                                1,
                                PeriodoFrecuencia.DIA),
                        ADMIN);
        var composicion =
                composiciones.crear(
                        catalogo.id(),
                        new ComposicionSuplementoRequest(
                                new BigDecimal("30"),
                                "G",
                                CORTE.minusDays(30),
                                null,
                                true,
                                "TEST"));
        composiciones.agregarComponente(
                composicion.getId(),
                new ComponenteComposicionSuplementoRequest(
                        TipoComponenteSuplemento.PROTEINA, null, new BigDecimal(proteina), "G"));
        if (conEquivalencia) {
            equivalencias.crear(
                    catalogo.id(),
                    new EquivalenciaUnidadSuplementoRequest(
                            BigDecimal.ONE,
                            "SCOOP",
                            new BigDecimal("30"),
                            "G",
                            CORTE.minusDays(30),
                            null,
                            true));
        }
        return asignacion.id();
    }

    private SuplementoCatalogoRequest catalogo(String nombre) {
        return new SuplementoCatalogoRequest(
                nombre + " " + UUID.randomUUID(),
                "GENERAL",
                null,
                null,
                null,
                "Marca",
                "Presentacion",
                "G",
                true);
    }

    private RegistroConsumoSuplementoRequest consumo(
            Long asignacionId, String cantidad, String unidad, int tomas) {
        return new RegistroConsumoSuplementoRequest(
                asignacionId, new BigDecimal(cantidad), unidad, tomas, null);
    }

    private RegistroHabito habito(Cliente cliente, LocalDate fecha) {
        RegistroHabito habito = new RegistroHabito();
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
        return habitos.save(habito);
    }

    @Autowired jakarta.persistence.EntityManager entityManager;

    private com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo entityManagerReference(
            Long id) {
        return entityManager.getReference(
                com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo.class, id);
    }
}
