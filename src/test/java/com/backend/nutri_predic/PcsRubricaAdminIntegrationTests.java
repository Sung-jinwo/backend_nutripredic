package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.consumo.classification.AmbitoAportePcs;
import com.backend.nutri_predic.consumo.classification.OperadorCriterioPcs;
import com.backend.nutri_predic.consumo.dto.CambioEstadoRubricaConsumoRequest;
import com.backend.nutri_predic.consumo.dto.CriterioConsumoSuplementosRequest;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoRequest;
import com.backend.nutri_predic.consumo.dto.RubricaConsumoSuplementosRequest;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.service.ConsumoEvaluacionExtractor;
import com.backend.nutri_predic.consumo.service.RubricaConsumoSuplementosService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PcsRubricaAdminIntegrationTests {
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);

    @Autowired RubricaConsumoSuplementosService service;
    @Autowired ConsumoEvaluacionExtractor evaluador;
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;

    @Test
    @Transactional
    void creaRubricaEnBorradorSinUmbrales() {
        var creada = service.crear(rubricaRequest(false, "Inicial"));

        assertThat(creada.id()).isNotNull();
        assertThat(creada.estado()).isEqualTo("BORRADOR");
        assertThat(creada.ventanaDias()).isEqualTo(7);
        assertThat(creada.reglaGlobal()).isNull();
        assertThat(creada.criterios()).isEmpty();
    }

    @Test
    @Transactional
    void editaRubricaYValidaVigencia() {
        var creada = service.crear(rubricaRequest(false, "Antes"));
        var editada = service.actualizar(creada.id(), rubricaRequest(true, "Después"));

        assertThat(editada.observacion()).isEqualTo("Después");
        assertThat(editada.validada()).isTrue();
        assertThat(editada.vigenteHasta()).isAfter(editada.vigenteDesde());
    }

    @Test
    @Transactional
    void agregaCriterioSinConvertirCantidadesNulasACero() {
        var rubrica = service.crear(rubricaRequest(false, null));
        var criterio = service.agregarCriterio(rubrica.id(), criterioRequest());

        assertThat(criterio.rubricaId()).isEqualTo(rubrica.id());
        assertThat(criterio.cantidadReferencia()).isNull();
        assertThat(criterio.numeroTomas()).isNull();
        assertThat(criterio.suplementoId()).isNull();
    }

    @Test
    @Transactional
    void impideActivarRubricaNoValidada() {
        var rubrica = service.crear(rubricaRequest(false, null));

        assertThatThrownBy(
                        () ->
                                service.cambiarEstado(
                                        rubrica.id(),
                                        new CambioEstadoRubricaConsumoRequest(
                                                EstadoCriterioConsumo.ACTIVO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no validada");
    }

    @Test
    @Transactional
    void obtieneRubricaConSusCriterios() {
        var rubrica = service.crear(rubricaRequest(false, null));
        var criterio = service.agregarCriterio(rubrica.id(), criterioEjecutableRequest());

        var obtenida = service.obtener(rubrica.id());

        assertThat(obtenida.criterios())
                .singleElement()
                .satisfies(
                        item -> {
                            assertThat(item.id()).isEqualTo(criterio.id());
                            assertThat(item.rubricaId()).isEqualTo(rubrica.id());
                            assertThat(item.alcance()).isEqualTo("SUPLEMENTO");
                            assertThat(item.componenteTipo()).isEqualTo("COMPONENTE_FICTICIO");
                            assertThat(item.ambitoAporte()).isEqualTo(AmbitoAportePcs.TOTAL_DIETA);
                            assertThat(item.fuenteReferencia()).isEqualTo("FUENTE_FICTICIA");
                            assertThat(item.versionReferencia()).isEqualTo("VERSION_FICTICIA");
                            assertThat(item.observacionMetodologica())
                                    .isEqualTo("OBSERVACION_FICTICIA");
                        });
    }

    @Test
    @Transactional
    void rubricaAplicableSinReglaEjecutableContinuaNoDeterminada() {
        var rubrica = service.crear(rubricaRequest(true, "Configuración pendiente"));
        service.cambiarEstado(
                rubrica.id(), new CambioEstadoRubricaConsumoRequest(EstadoCriterioConsumo.ACTIVO));
        var registro =
                auth.register(
                        new RegisterRequest(
                                "pcs-admin-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "PCS"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();

        var resultado =
                evaluador.extraer(new EvaluacionConsumoRequest(cliente.getId(), CORTE, 7, null));

        assertThat(resultado.rubricaId()).isEqualTo(rubrica.id());
        assertThat(resultado.altoConsumo()).isNull();
        assertThat(resultado.estadoClasificacion()).isEqualTo("NO_DETERMINADA");
        assertThat(resultado.motivo()).isEqualTo("CRITERIO_NO_IMPLEMENTADO");
    }

    private RubricaConsumoSuplementosRequest rubricaRequest(boolean validada, String observacion) {
        Instant desde = CORTE.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant hasta = CORTE.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return new RubricaConsumoSuplementosRequest(
                "PCS-ADMIN-" + UUID.randomUUID(),
                1,
                7,
                desde,
                hasta,
                validada,
                observacion,
                null,
                validada ? "metodologia@test" : null,
                validada ? desde : null,
                null,
                null,
                null);
    }

    private CriterioConsumoSuplementosRequest criterioRequest() {
        return new CriterioConsumoSuplementosRequest(
                "SUPLEMENTO",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);
    }

    private CriterioConsumoSuplementosRequest criterioEjecutableRequest() {
        return new CriterioConsumoSuplementosRequest(
                "SUPLEMENTO",
                null,
                "COMPONENTE_FICTICIO",
                null,
                "APORTE_COMPONENTE",
                new BigDecimal("100"),
                null,
                AmbitoAportePcs.TOTAL_DIETA,
                "U_FICTICIA",
                "U_FICTICIA",
                null,
                null,
                7,
                true,
                OperadorCriterioPcs.MAYOR_QUE,
                null,
                "FUENTE_FICTICIA",
                "VERSION_FICTICIA",
                "OBSERVACION_FICTICIA");
    }
}
