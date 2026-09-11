package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.consumo.classification.AmbitoAportePcs;
import com.backend.nutri_predic.consumo.classification.ExcesoComponentePcsAggregationRule;
import com.backend.nutri_predic.consumo.classification.OperadorCriterioPcs;
import com.backend.nutri_predic.consumo.classification.PcsClassificationEngine;
import com.backend.nutri_predic.consumo.classification.PcsComponentContribution;
import com.backend.nutri_predic.consumo.classification.PcsFactualSnapshot;
import com.backend.nutri_predic.consumo.classification.ResultadoCriterioPcs;
import com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExcesoComponentePcsClassificationTests {
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);
    private static final String UNIDAD = "U_FICTICIA";
    private final PcsClassificationEngine engine =
            new PcsClassificationEngine(List.of(new ExcesoComponentePcsAggregationRule()));

    @Test
    void total110Supera100YEsAlto() {
        var resultado =
                evaluar(
                        List.of(criterio("C1", AmbitoAportePcs.TOTAL_DIETA)),
                        List.of(aporte("C1", "60", true, "50", true)));
        assertThat(resultado.altoConsumo()).isTrue();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("ALTO");
        assertThat(resultado.trazas())
                .singleElement()
                .satisfies(
                        t -> {
                            assertThat(t.aporteAlimentos()).isEqualByComparingTo("60");
                            assertThat(t.aporteSuplementos()).isEqualByComparingTo("50");
                            assertThat(t.valorObservadoFinal()).isEqualByComparingTo("110");
                            assertThat(t.resultado()).isEqualTo(ResultadoCriterioPcs.CUMPLE);
                        });
    }

    @Test
    void total70NoSupera100YEsNoAlto() {
        var resultado =
                evaluar(
                        List.of(criterio("C1", AmbitoAportePcs.TOTAL_DIETA)),
                        List.of(aporte("C1", "30", true, "40", true)));
        assertThat(resultado.altoConsumo()).isFalse();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("NO_ALTO");
    }

    @Test
    void totalDietaSinAlimentosEsNoCalculable() {
        var resultado =
                evaluar(
                        List.of(criterio("C1", AmbitoAportePcs.TOTAL_DIETA)),
                        List.of(aporte("C1", null, false, "40", true)));
        assertThat(resultado.altoConsumo()).isNull();
        assertThat(resultado.trazas())
                .singleElement()
                .satisfies(
                        t -> {
                            assertThat(t.resultado()).isEqualTo(ResultadoCriterioPcs.NO_CALCULABLE);
                            assertThat(t.motivoNoCalculable()).isEqualTo("APORTE_TOTAL_INCOMPLETO");
                        });
    }

    @Test
    void soloSuplementosNoRequiereAlimentacion() {
        var resultado =
                evaluar(
                        List.of(criterio("C1", AmbitoAportePcs.SOLO_SUPLEMENTOS)),
                        List.of(aporte("C1", null, false, "110", true)));
        assertThat(resultado.altoConsumo()).isTrue();
        assertThat(resultado.trazas().getFirst().valorObservadoFinal()).isEqualByComparingTo("110");
    }

    @Test
    void unExcesoPrevaleceSobreOtroCriterioNoCalculable() {
        var resultado =
                evaluar(
                        List.of(
                                criterio("C1", AmbitoAportePcs.SOLO_SUPLEMENTOS),
                                criterio("C2", AmbitoAportePcs.TOTAL_DIETA)),
                        List.of(
                                aporte("C1", null, false, "110", true),
                                aporte("C2", null, false, "40", true)));
        assertThat(resultado.altoConsumo()).isTrue();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("ALTO");
    }

    @Test
    void sinExcesoYConNoCalculableNoProduceNoAlto() {
        var resultado =
                evaluar(
                        List.of(
                                criterio("C1", AmbitoAportePcs.SOLO_SUPLEMENTOS),
                                criterio("C2", AmbitoAportePcs.TOTAL_DIETA)),
                        List.of(
                                aporte("C1", null, false, "40", true),
                                aporte("C2", null, false, "40", true)));
        assertThat(resultado.altoConsumo()).isNull();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("NO_DETERMINADA");
    }

    @Test
    void todosNoCumpleCalculablesProduceNoAlto() {
        var resultado =
                evaluar(
                        List.of(
                                criterio("C1", AmbitoAportePcs.SOLO_SUPLEMENTOS),
                                criterio("C2", AmbitoAportePcs.TOTAL_DIETA)),
                        List.of(
                                aporte("C1", null, false, "40", true),
                                aporte("C2", "30", true, "40", true)));
        assertThat(resultado.altoConsumo()).isFalse();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("NO_ALTO");
    }

    @Test
    void sinCriteriosAplicablesEsNoDeterminada() {
        var resultado = evaluar(List.of(), List.of(aporte("C1", "60", true, "50", true)));
        assertThat(resultado.altoConsumo()).isNull();
        assertThat(resultado.estadoClasificacion().name()).isEqualTo("NO_DETERMINADA");
        assertThat(resultado.motivo()).isEqualTo("SIN_CRITERIOS_APLICABLES");
    }

    private com.backend.nutri_predic.consumo.classification.PcsEvaluationResult evaluar(
            List<CriterioConsumoSuplementos> criterios, List<PcsComponentContribution> aportes) {
        return engine.evaluar(
                new PcsFactualSnapshot(CORTE, 7, List.of(), aportes), rubrica(), criterios);
    }

    private CriterioConsumoSuplementos criterio(String componente, AmbitoAportePcs ambito) {
        var criterio = new CriterioConsumoSuplementos();
        criterio.setAlcance("COMPONENTE_FICTICIO");
        criterio.setComponenteTipo(componente);
        criterio.setCantidadReferencia(new BigDecimal("100"));
        criterio.setUnidadNormalizada(UNIDAD);
        criterio.setVentanaDias(7);
        criterio.setAmbitoAporte(ambito);
        criterio.setOperador(OperadorCriterioPcs.MAYOR_QUE);
        return criterio;
    }

    private PcsComponentContribution aporte(
            String componente,
            String alimentos,
            boolean alimentosCalculables,
            String suplementos,
            boolean suplementosCalculables) {
        return new PcsComponentContribution(
                componente,
                alimentos == null ? null : new BigDecimal(alimentos),
                alimentosCalculables,
                suplementos == null ? null : new BigDecimal(suplementos),
                suplementosCalculables,
                UNIDAD);
    }

    private RubricaConsumoSuplementos rubrica() {
        var rubrica = new RubricaConsumoSuplementos();
        rubrica.setCodigo("PCS-FICTICIA");
        rubrica.setVersion(1);
        rubrica.setEstado(EstadoCriterioConsumo.ACTIVO);
        rubrica.setValidada(true);
        rubrica.setReglaGlobal(ExcesoComponentePcsAggregationRule.CODIGO);
        return rubrica;
    }
}
