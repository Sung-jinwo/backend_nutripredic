package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.consumo.classification.OperadorCriterioPcs;
import com.backend.nutri_predic.consumo.classification.PcsClassificationEngine;
import com.backend.nutri_predic.consumo.classification.PcsFactualSnapshot;
import com.backend.nutri_predic.consumo.classification.PcsObservedValue;
import com.backend.nutri_predic.consumo.classification.ResultadoCriterioPcs;
import com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class PcsClassificationEngineTests {
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);
    private final PcsClassificationEngine engine = new PcsClassificationEngine(List.of());

    @Test
    void criterioSinValorReferenciaEsNoCalculable() {
        var criterio = criterio(OperadorCriterioPcs.MAYOR_QUE, null, "U_FICTICIA");

        var resultado =
                engine.evaluar(
                        snapshot(new BigDecimal("17"), "U_FICTICIA"), rubrica(), List.of(criterio));

        assertThat(resultado.trazas())
                .singleElement()
                .extracting(traza -> traza.resultado())
                .isEqualTo(ResultadoCriterioPcs.NO_CALCULABLE);
        assertThat(resultado.criteriosNoCalculables()).isEqualTo(1);
    }

    @Test
    void datoFactualNuloEsNoCalculableYNoSeConvierteACero() {
        var resultado =
                engine.evaluar(
                        snapshot(null, "U_FICTICIA"),
                        rubrica(),
                        List.of(
                                criterio(
                                        OperadorCriterioPcs.MAYOR_QUE,
                                        new BigDecimal("11"),
                                        "U_FICTICIA")));

        assertThat(resultado.trazas())
                .singleElement()
                .satisfies(
                        traza -> {
                            assertThat(traza.datoObservado()).isNull();
                            assertThat(traza.resultado())
                                    .isEqualTo(ResultadoCriterioPcs.NO_CALCULABLE);
                        });
    }

    @Test
    void unidadIncompatibleEsNoCalculable() {
        var resultado =
                engine.evaluar(
                        snapshot(new BigDecimal("17"), "UNIDAD_A"),
                        rubrica(),
                        List.of(
                                criterio(
                                        OperadorCriterioPcs.MAYOR_O_IGUAL,
                                        new BigDecimal("11"),
                                        "UNIDAD_B")));

        assertThat(resultado.trazas())
                .singleElement()
                .extracting(traza -> traza.resultado())
                .isEqualTo(ResultadoCriterioPcs.NO_CALCULABLE);
    }

    @Test
    void operadorTecnicoValidoEvaluaDatosFicticios() {
        var resultado =
                engine.evaluar(
                        snapshot(new BigDecimal("17"), "U_FICTICIA"),
                        rubrica(),
                        List.of(
                                criterio(
                                        OperadorCriterioPcs.MAYOR_QUE,
                                        new BigDecimal("11"),
                                        "U_FICTICIA")));

        assertThat(resultado.trazas())
                .singleElement()
                .satisfies(
                        traza -> {
                            assertThat(traza.operador()).isEqualTo(OperadorCriterioPcs.MAYOR_QUE);
                            assertThat(traza.valorReferencia()).isEqualByComparingTo("11");
                            assertThat(traza.resultado()).isEqualTo(ResultadoCriterioPcs.CUMPLE);
                        });
        assertThat(resultado.criteriosEvaluados()).isEqualTo(1);
        assertThat(resultado.criteriosCumplidos()).isEqualTo(1);
    }

    @Test
    void ausenciaDeReglaAgregacionMantieneClasificacionNoDeterminada() {
        var rubrica = rubrica();
        rubrica.setReglaGlobal("AGREGACION_FICTICIA_NO_IMPLEMENTADA");

        var resultado =
                engine.evaluar(
                        snapshot(new BigDecimal("17"), "U_FICTICIA"),
                        rubrica,
                        List.of(
                                criterio(
                                        OperadorCriterioPcs.MENOR_QUE,
                                        new BigDecimal("20"),
                                        "U_FICTICIA")));

        assertThat(resultado.altoConsumo()).isNull();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("NO_DETERMINADA");
        assertThat(resultado.motivo()).isEqualTo("CRITERIO_NO_IMPLEMENTADO");
    }

    private RubricaConsumoSuplementos rubrica() {
        var rubrica = new RubricaConsumoSuplementos();
        rubrica.setCodigo("PCS-FICTICIA");
        rubrica.setVersion(1);
        rubrica.setEstado(EstadoCriterioConsumo.ACTIVO);
        rubrica.setValidada(true);
        rubrica.setVigenteDesde(CORTE.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        rubrica.setVigenteHasta(CORTE.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        return rubrica;
    }

    private CriterioConsumoSuplementos criterio(
            OperadorCriterioPcs operador, BigDecimal referencia, String unidad) {
        var criterio = new CriterioConsumoSuplementos();
        criterio.setAlcance("FICTICIO");
        criterio.setMetrica("METRICA_FICTICIA");
        criterio.setOperador(operador);
        criterio.setCantidadReferencia(referencia);
        criterio.setUnidadNormalizada(unidad);
        return criterio;
    }

    private PcsFactualSnapshot snapshot(BigDecimal valor, String unidad) {
        return new PcsFactualSnapshot(
                CORTE,
                List.of(
                        new PcsObservedValue(
                                null, null, null, "METRICA_FICTICIA", valor, unidad, true)));
    }
}
