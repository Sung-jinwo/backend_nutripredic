package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import com.backend.nutri_predic.alimentacion.service.AgregacionNutricionalService;
import com.backend.nutri_predic.alimentacion.service.AlimentacionService;
import com.backend.nutri_predic.alimentacion.service.ComposicionNutricionalAlimentoService;
import com.backend.nutri_predic.alimentacion.service.EquivalenciaUnidadAlimentoService;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.dto.ClienteRequest;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.service.ClienteService;
import com.backend.nutri_predic.common.enums.EstadoCliente;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class AgregacionNutricionalIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired ClienteService clienteService;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired AlimentacionService alimentacion;
    @Autowired ComposicionNutricionalAlimentoService composiciones;
    @Autowired EquivalenciaUnidadAlimentoService equivalencias;
    @Autowired AgregacionNutricionalService agregacion;
    private final UsernamePasswordAuthenticationToken admin =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Test
    @Transactional
    void agregaVentanaHistoricaConCoberturaConversionYNulos() {
        var cliente =
                clientes.findById(
                                auth.register(
                                                new RegisterRequest(
                                                        "agregacion-"
                                                                + UUID.randomUUID()
                                                                + "@test.local",
                                                        "Password1!",
                                                        "Cliente"))
                                        .clienteId())
                        .orElseThrow();
        var corte = LocalDate.now(java.time.ZoneId.of("America/Lima"));
        clienteService.update(
                cliente.getId(),
                new ClienteRequest(
                        30,
                        new BigDecimal("70"),
                        new BigDecimal("170"),
                        "Mantener",
                        null,
                        EstadoCliente.ACTIVO),
                admin);
        var avena =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Avena " + UUID.randomUUID(), "CEREAL", "G", true));
        var pollo =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Pollo " + UUID.randomUUID(), "PROTEINA", "G", true));
        var sinComposicion =
                alimentacion.guardarCatalogo(
                        null,
                        new AlimentoCatalogoRequest(
                                "Sin composición " + UUID.randomUUID(), "TECNICO", "G", true));
        composiciones.crear(
                avena.id(),
                new ComposicionNutricionalAlimentoRequest(
                        new BigDecimal("100"),
                        "G",
                        new BigDecimal("400"),
                        new BigDecimal("20"),
                        new BigDecimal("60"),
                        new BigDecimal("10"),
                        new BigDecimal("8"),
                        null,
                        null,
                        "fixture",
                        corte.minusDays(10),
                        null,
                        true));
        composiciones.crear(
                pollo.id(),
                new ComposicionNutricionalAlimentoRequest(
                        new BigDecimal("100"),
                        "G",
                        new BigDecimal("165"),
                        new BigDecimal("31"),
                        BigDecimal.ZERO,
                        new BigDecimal("3.6"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        "fixture",
                        corte.minusDays(10),
                        null,
                        true));
        equivalencias.crear(
                pollo.id(),
                new EquivalenciaUnidadAlimentoRequest(
                        BigDecimal.ONE,
                        "UNIDAD",
                        new BigDecimal("50"),
                        "G",
                        "fixture",
                        corte.minusDays(10),
                        null,
                        true));
        var diaUno = habito(cliente, corte);
        var diaDos = habito(cliente, corte.minusDays(2));
        habito(cliente, corte.minusDays(1));
        var futuro = habito(cliente, corte.plusDays(1));
        var antiguo = habito(cliente, corte.minusDays(7));
        alimentacion.agregarRegistro(
                diaUno.getId(),
                new RegistroAlimentoRequest(
                        avena.id(), new BigDecimal("50"), "G", MomentoComida.DESAYUNO),
                admin);
        alimentacion.agregarRegistro(
                diaUno.getId(),
                new RegistroAlimentoRequest(
                        pollo.id(), new BigDecimal("2"), "UNIDAD", MomentoComida.ALMUERZO),
                admin);
        alimentacion.agregarRegistro(
                diaUno.getId(),
                new RegistroAlimentoRequest(
                        sinComposicion.id(), BigDecimal.ONE, "G", MomentoComida.CENA),
                admin);
        alimentacion.agregarRegistro(
                diaDos.getId(),
                new RegistroAlimentoRequest(
                        avena.id(), new BigDecimal("100"), "G", MomentoComida.DESAYUNO),
                admin);
        alimentacion.agregarRegistro(
                futuro.getId(),
                new RegistroAlimentoRequest(
                        avena.id(), new BigDecimal("100"), "G", MomentoComida.DESAYUNO),
                admin);
        alimentacion.agregarRegistro(
                antiguo.getId(),
                new RegistroAlimentoRequest(
                        avena.id(), new BigDecimal("100"), "G", MomentoComida.DESAYUNO),
                admin);

        var resumen = agregacion.resumir(cliente.getId(), corte);
        var diario =
                resumen.dias().stream()
                        .filter(d -> d.fecha().equals(corte))
                        .findFirst()
                        .orElseThrow();
        assertThat(resumen.dias()).hasSize(7);
        assertThat(diario.kcal().valorConocido()).isEqualByComparingTo("365");
        assertThat(diario.proteinaG().valorConocido()).isEqualByComparingTo("41");
        assertThat(diario.registrosAlimentoNoCalculables()).isEqualTo(1);
        assertThat(diario.kcal().coberturaPorcentaje()).isEqualByComparingTo("66.67");
        assertThat(diario.azucarG().valorConocido()).isZero();
        assertThat(diario.azucarG().completo()).isFalse();
        assertThat(
                        resumen.dias().stream()
                                .filter(d -> d.fecha().equals(corte.minusDays(1)))
                                .findFirst()
                                .orElseThrow()
                                .kcal()
                                .valorConocido())
                .isNull();
        assertThat(resumen.ventana().totalRegistrosAlimento()).isEqualTo(4);
        assertThat(resumen.ventana().registrosCalculables()).isEqualTo(3);
        assertThat(resumen.ventana().registrosNoCalculables()).isEqualTo(1);
        assertThat(resumen.ventana().kcal().totalConocido()).isEqualByComparingTo("765");
        assertThat(resumen.ventana().kcal().promedioSobreVentanaConocido())
                .isEqualByComparingTo("109.285714");
        assertThat(resumen.ventana().kcal().promedioSobreDiasConNutricionConocido())
                .isEqualByComparingTo("382.5");
        assertThat(resumen.ventana().proteinaGKgDiaSobreDiasConNutricionConocido())
                .isEqualByComparingTo("0.435714");
        assertThat(resumen.ventana().fuentePeso()).isEqualTo("HISTORIAL_PERFIL");
    }

    private RegistroHabito habito(
            com.backend.nutri_predic.cliente.entity.Cliente cliente, LocalDate fecha) {
        var h = new RegistroHabito();
        h.setCliente(cliente);
        h.setFecha(fecha);
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
