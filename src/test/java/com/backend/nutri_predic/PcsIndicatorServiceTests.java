package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.consumo.entity.EstadoClasificacionConsumo;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.EvaluacionConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.repository.RubricaConsumoSuplementosRepository;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcs;
import com.backend.nutri_predic.indicador.service.PcsIndicatorService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PcsIndicatorServiceTests {
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);

    @Autowired PcsIndicatorService pcs;
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired EvaluacionConsumoRepository evaluaciones;
    @Autowired RubricaConsumoSuplementosRepository rubricas;

    @Test
    @Transactional
    void sinEvaluacionesPcsEsNoDisponible() {
        var resultado = pcs.obtener();

        assertNoDisponible(resultado);
        assertThat(resultado.totalEvaluadosValidos()).isZero();
        assertThat(resultado.totalAltoConsumo()).isZero();
    }

    @Test
    @Transactional
    void soloEvaluacionNoDeterminadaNoHabilitaPcs() {
        guardar(
                cliente(),
                EstadoValidezMedicion.NO_DETERMINADA,
                EstadoClasificacionConsumo.NO_DETERMINADA,
                null);

        var resultado = pcs.obtener();

        assertNoDisponible(resultado);
        assertThat(resultado.totalEvaluadosValidos()).isZero();
    }

    @Test
    @Transactional
    void sinNivelManualPcsOficialSigueNoDisponible() {
        // Cliente ya no porta nivel manual: el PCS oficial solo depende de
        // evaluaciones_consumo y su rúbrica.
        cliente();

        var resultado = pcs.obtener();

        assertNoDisponible(resultado);
        assertThat(resultado.totalAltoConsumo()).isZero();
    }

    @Test
    @Transactional
    void evaluacionInvalidaNoCuenta() {
        guardar(cliente(), EstadoValidezMedicion.INVALIDA, EstadoClasificacionConsumo.ALTO, true);

        var resultado = pcs.obtener();

        assertNoDisponible(resultado);
        assertThat(resultado.totalEvaluadosValidos()).isZero();
        assertThat(resultado.totalAltoConsumo()).isZero();
    }

    @Test
    @Transactional
    void ausenciaDeDatosNoSeConvierteEnCeroPorCiento() {
        var resultado = pcs.obtener();

        assertThat(resultado.porcentajePcs()).isNull();
        assertThat(resultado.estadoDisponibilidad())
                .isEqualTo(EstadoDisponibilidadPcs.NO_DISPONIBLE);
    }

    private void assertNoDisponible(
            com.backend.nutri_predic.indicador.dto.PcsIndicatorResponse resultado) {
        assertThat(resultado.porcentajePcs()).isNull();
        assertThat(resultado.estadoDisponibilidad())
                .isEqualTo(EstadoDisponibilidadPcs.NO_DISPONIBLE);
        assertThat(resultado.motivoNoDisponible())
                .isEqualTo("CLASIFICACION_METODOLOGICA_NO_DISPONIBLE");
    }

    private void guardar(
            Cliente cliente,
            EstadoValidezMedicion validez,
            EstadoClasificacionConsumo clasificacion,
            Boolean altoConsumo) {
        var evaluacion = new EvaluacionConsumo();
        evaluacion.setCliente(cliente);
        evaluacion.setRubrica(rubrica());
        evaluacion.setFechaInicio(CORTE.minusDays(6));
        evaluacion.setFechaCorte(CORTE);
        evaluacion.setVentanaDias(7);
        evaluacion.setEstadoValidez(validez);
        evaluacion.setEstadoClasificacion(clasificacion);
        evaluacion.setAltoConsumo(altoConsumo);
        evaluacion.setSchemaVersion("pcs-indicador-test-v1");
        evaluaciones.saveAndFlush(evaluacion);
    }

    private RubricaConsumoSuplementos rubrica() {
        var rubrica = new RubricaConsumoSuplementos();
        rubrica.setCodigo("PCS-INDICADOR-" + UUID.randomUUID());
        rubrica.setVersion(1);
        rubrica.setEstado(EstadoCriterioConsumo.ACTIVO);
        rubrica.setValidada(true);
        rubrica.setVentanaDias(7);
        return rubricas.save(rubrica);
    }

    private Cliente cliente() {
        var registro =
                auth.register(
                        new RegisterRequest(
                                "pcs-indicador-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "PCS"));
        return clientes.findById(registro.clienteId()).orElseThrow();
    }
}
