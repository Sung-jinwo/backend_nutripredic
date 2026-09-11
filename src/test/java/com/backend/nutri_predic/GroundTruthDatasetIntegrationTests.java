package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.*;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.datasetmodelov5.service.DatasetModeloV5Service;
import com.backend.nutri_predic.perfilhabitos.dto.*;
import com.backend.nutri_predic.perfilhabitos.entity.*;
import com.backend.nutri_predic.perfilhabitos.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class GroundTruthDatasetIntegrationTests {
    @Autowired RubricaPerfilHabitosService rubricas;
    @Autowired EvaluacionPerfilHabitosService evaluaciones;
    @Autowired DatasetModeloV5Service datasetV5;
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    private final UsernamePasswordAuthenticationToken admin =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Test
    @Transactional
    void clasificacionSeDerivaDeCriteriosConfigurados() {
        var rubrica = activarRubrica();
        var cliente = cliente("cortes");
        assertThat(
                        evaluar(cliente, rubrica.id(), "70.00", EstadoValidezMedicion.VALIDA)
                                .clasificacionReal())
                .isEqualTo("ADECUADO");
        assertThat(
                        evaluar(cliente, rubrica.id(), "69.99", EstadoValidezMedicion.VALIDA)
                                .clasificacionReal())
                .isEqualTo("MEJORABLE");
        assertThat(
                        evaluar(cliente, rubrica.id(), "40.00", EstadoValidezMedicion.VALIDA)
                                .clasificacionReal())
                .isEqualTo("MEJORABLE");
        assertThat(
                        evaluar(cliente, rubrica.id(), "39.99", EstadoValidezMedicion.VALIDA)
                                .clasificacionReal())
                .isEqualTo("CRITICO");
    }

    @Test
    @Transactional
    void borradorNoEntraYVersionActivaEsInmutable() {
        var borrador = crearRubrica();
        var cliente = cliente("borrador");
        agregarCriterios(borrador.id());
        assertThatThrownBy(() -> evaluar(cliente, borrador.id(), "80.00", EstadoValidezMedicion.VALIDA))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Sólo se puede evaluar con una rúbrica ACTIVA");
        rubricas.cambiarEstado(
                borrador.id(), new CambioEstadoRubricaRequest(EstadoRubricaPerfilHabitos.ACTIVA));
        assertThatThrownBy(
                        () ->
                                rubricas.agregarCriterio(
                                        borrador.id(),
                                        criterio(
                                                ClasificacionPerfilHabitos.ADECUADO,
                                                "70",
                                                "100",
                                                true,
                                                true,
                                                4)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Transactional
    void datasetV5NoExportaGroundTruthConXIncompletas() {
        var rubrica = activarRubrica();
        var cliente = cliente("dataset-v5");
        evaluar(cliente, rubrica.id(), "80.00", EstadoValidezMedicion.VALIDA);
        evaluar(cliente, rubrica.id(), "30.00", EstadoValidezMedicion.INVALIDA);

        assertThat(datasetV5.filas()).isEmpty();
        assertThat(datasetV5.csv().lines()).hasSize(1);
        assertThat(datasetV5.csv())
                .contains("case_id", "rubrica_codigo", "rubrica_version", "clasificacion_real")
                .doesNotContain("cliente_id", "evaluacion_id", "ADECUADO");
        assertThat(datasetV5.calidad().totalEvaluacionesValidas()).isEqualTo(1);
        assertThat(datasetV5.calidad().totalFilasExportadas()).isZero();
    }

    private RubricaPerfilHabitosResponse activarRubrica() {
        var r = crearRubrica();
        agregarCriterios(r.id());
        return rubricas.cambiarEstado(
                r.id(), new CambioEstadoRubricaRequest(EstadoRubricaPerfilHabitos.ACTIVA));
    }

    private RubricaPerfilHabitosResponse crearRubrica() {
        return rubricas.crear(
                new RubricaPerfilHabitosRequest(
                        "PERFIL-" + UUID.randomUUID(),
                        1,
                        "Perfil de hábitos",
                        null,
                        Instant.now(),
                        "Especialista",
                        Instant.now()));
    }

    private void agregarCriterios(Long id) {
        rubricas.agregarCriterio(
                id, criterio(ClasificacionPerfilHabitos.CRITICO, "0", "40", true, false, 1));
        rubricas.agregarCriterio(
                id, criterio(ClasificacionPerfilHabitos.MEJORABLE, "40", "70", true, false, 2));
        rubricas.agregarCriterio(
                id, criterio(ClasificacionPerfilHabitos.ADECUADO, "70", "100", true, true, 3));
    }

    private CriterioClasificacionPerfilRequest criterio(
            ClasificacionPerfilHabitos c, String min, String max, boolean i, boolean s, int orden) {
        return new CriterioClasificacionPerfilRequest(
                c, new BigDecimal(min), new BigDecimal(max), i, s, orden);
    }

    private EvaluacionPerfilHabitosResponse evaluar(
            Cliente c, Long rubricaId, String puntaje, EstadoValidezMedicion validez) {
        return evaluaciones.crear(
                new EvaluacionPerfilHabitosRequest(
                        c.getId(),
                        rubricaId,
                        LocalDate.now(),
                        new BigDecimal(puntaje),
                        null,
                        null,
                        validez,
                        null,
                        null,
                        null,
                        null),
                admin);
    }

    private Cliente cliente(String prefijo) {
        var registro =
                auth.register(
                        new RegisterRequest(
                                prefijo + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Cliente Dataset"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();
        cliente.setEdad(28);
        cliente.setPesoKg(new BigDecimal("65"));
        cliente.setAlturaCm(new BigDecimal("168"));
        cliente.setObjetivoFisico("Mejorar");
        return clientes.save(cliente);
    }
}
