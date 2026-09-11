package com.backend.nutri_predic.consumo.classification;

import com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos;
import com.backend.nutri_predic.consumo.entity.EstadoClasificacionConsumo;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PcsClassificationEngine {
    public static final String CRITERIO_NO_CONFIGURADO = "CRITERIO_NO_CONFIGURADO";
    public static final String CRITERIO_NO_IMPLEMENTADO = "CRITERIO_NO_IMPLEMENTADO";

    private final List<PcsAggregationRule> reglasAgregacion;

    public PcsClassificationEngine(List<PcsAggregationRule> reglasAgregacion) {
        this.reglasAgregacion = List.copyOf(reglasAgregacion);
    }

    public PcsEvaluationResult evaluar(
            PcsFactualSnapshot snapshot,
            RubricaConsumoSuplementos rubrica,
            List<CriterioConsumoSuplementos> criterios) {
        if (!aplicable(rubrica, snapshot)) {
            return resultadoNoDeterminado(snapshot, rubrica, List.of(), CRITERIO_NO_CONFIGURADO);
        }

        List<PcsCriterionTrace> trazas =
                criterios == null
                        ? List.of()
                        : criterios.stream()
                                .map(criterio -> evaluarCriterio(snapshot, criterio))
                                .toList();
        if (rubrica.getReglaGlobal() == null || rubrica.getReglaGlobal().isBlank()) {
            return resultadoNoDeterminado(snapshot, rubrica, trazas, CRITERIO_NO_IMPLEMENTADO);
        }
        if (trazas.isEmpty()) {
            return resultadoNoDeterminado(snapshot, rubrica, trazas, "SIN_CRITERIOS_APLICABLES");
        }
        var regla =
                reglasAgregacion.stream()
                        .filter(item -> item.soporta(rubrica.getReglaGlobal()))
                        .findFirst();
        if (regla.isEmpty()) {
            return resultadoNoDeterminado(snapshot, rubrica, trazas, CRITERIO_NO_IMPLEMENTADO);
        }

        PcsAggregationResult agregado = regla.get().agregar(rubrica.getReglaGlobal(), trazas);
        if (agregado == null || !agregado.calculable() || agregado.altoConsumo() == null) {
            return resultadoNoDeterminado(
                    snapshot,
                    rubrica,
                    trazas,
                    agregado == null || agregado.motivo() == null
                            ? CRITERIO_NO_IMPLEMENTADO
                            : agregado.motivo());
        }
        return resultado(
                snapshot,
                rubrica,
                trazas,
                agregado.altoConsumo(),
                agregado.altoConsumo()
                        ? EstadoClasificacionConsumo.ALTO
                        : EstadoClasificacionConsumo.NO_ALTO,
                null);
    }

    private PcsCriterionTrace evaluarCriterio(
            PcsFactualSnapshot snapshot, CriterioConsumoSuplementos criterio) {
        if (criterio.getAmbitoAporte() != null) {
            return evaluarComponente(snapshot, criterio);
        }
        return evaluarObservacionLegacy(snapshot, criterio);
    }

    private PcsCriterionTrace evaluarComponente(
            PcsFactualSnapshot snapshot, CriterioConsumoSuplementos criterio) {
        if (criterio.getComponenteTipo() == null
                || criterio.getComponenteTipo().isBlank()
                || criterio.getOperador() == null
                || criterio.getCantidadReferencia() == null
                || criterio.getOperador() == OperadorCriterioPcs.ENTRE
                        && (criterio.getCantidadReferenciaHasta() == null
                                || criterio.getCantidadReferencia()
                                                .compareTo(criterio.getCantidadReferenciaHasta())
                                        > 0)
                || criterio.getVentanaDias() != null
                        && !criterio.getVentanaDias().equals(snapshot.ventanaDias())) {
            return noCalculable(
                    criterio, (PcsComponentContribution) null, "CONFIGURACION_INCOMPLETA");
        }
        List<PcsComponentContribution> aportes =
                snapshot.componentes().stream()
                        .filter(a -> criterio.getComponenteTipo().equalsIgnoreCase(a.componente()))
                        .toList();
        if (aportes.size() != 1) {
            return noCalculable(
                    criterio,
                    aportes.isEmpty() ? null : aportes.getFirst(),
                    aportes.isEmpty() ? "COMPONENTE_SIN_DATOS" : "COMPONENTE_AMBIGUO");
        }
        PcsComponentContribution aporte = aportes.getFirst();
        if (!unidadCompatible(criterio, aporte.unidad())) {
            return noCalculable(criterio, aporte, "UNIDAD_INCOMPATIBLE");
        }
        BigDecimal observado;
        if (criterio.getAmbitoAporte() == AmbitoAportePcs.TOTAL_DIETA) {
            if (!aporte.alimentosCalculables()
                    || aporte.aporteAlimentos() == null
                    || !aporte.suplementosCalculables()
                    || aporte.aporteSuplementos() == null) {
                return noCalculable(criterio, aporte, "APORTE_TOTAL_INCOMPLETO");
            }
            observado = aporte.aporteAlimentos().add(aporte.aporteSuplementos());
        } else {
            if (!aporte.suplementosCalculables() || aporte.aporteSuplementos() == null) {
                return noCalculable(criterio, aporte, "APORTE_SUPLEMENTOS_NO_CALCULABLE");
            }
            observado = aporte.aporteSuplementos();
        }
        boolean cumple =
                comparar(
                        observado,
                        criterio.getOperador(),
                        criterio.getCantidadReferencia(),
                        criterio.getCantidadReferenciaHasta());
        return traza(
                criterio,
                aporte,
                observado,
                cumple ? ResultadoCriterioPcs.CUMPLE : ResultadoCriterioPcs.NO_CUMPLE,
                null);
    }

    private PcsCriterionTrace evaluarObservacionLegacy(
            PcsFactualSnapshot snapshot, CriterioConsumoSuplementos criterio) {
        OperadorCriterioPcs operador = criterio.getOperador();
        BigDecimal referencia = criterio.getCantidadReferencia();
        if (operador == null
                || referencia == null
                || operador == OperadorCriterioPcs.ENTRE
                        && (criterio.getCantidadReferenciaHasta() == null
                                || referencia.compareTo(criterio.getCantidadReferenciaHasta())
                                        > 0)) {
            return noCalculable(criterio, (PcsObservedValue) null, "CONFIGURACION_INCOMPLETA");
        }

        List<PcsObservedValue> coincidencias =
                snapshot.valores().stream().filter(valor -> coincide(criterio, valor)).toList();
        if (coincidencias.size() != 1) {
            return noCalculable(
                    criterio,
                    coincidencias.isEmpty() ? null : coincidencias.getFirst(),
                    "DATO_NO_UNICO");
        }
        PcsObservedValue observado = coincidencias.getFirst();
        if (observado.datoObservado() == null
                || criterio.isRequiereComposicion() && !observado.normalizado()
                || !unidadCompatible(criterio, observado)) {
            return noCalculable(criterio, observado, "DATO_O_UNIDAD_NO_CALCULABLE");
        }

        boolean cumple =
                comparar(
                        observado.datoObservado(),
                        operador,
                        referencia,
                        criterio.getCantidadReferenciaHasta());
        return new PcsCriterionTrace(
                criterio.getId(),
                criterio.getComponenteTipo(),
                null,
                null,
                observado.datoObservado(),
                null,
                observado.unidad(),
                operador,
                referencia,
                criterio.getCantidadReferenciaHasta(),
                cumple ? ResultadoCriterioPcs.CUMPLE : ResultadoCriterioPcs.NO_CUMPLE,
                null);
    }

    private boolean coincide(CriterioConsumoSuplementos criterio, PcsObservedValue valor) {
        return (criterio.getSuplemento() == null
                        || criterio.getSuplemento().getId().equals(valor.suplementoId()))
                && igualOpcional(criterio.getComponenteTipo(), valor.componenteTipo())
                && igualOpcional(criterio.getElementoAplicable(), valor.elementoAplicable())
                && igualOpcional(criterio.getMetrica(), valor.metrica());
    }

    private boolean igualOpcional(String esperado, String actual) {
        return esperado == null
                || esperado.isBlank()
                || actual != null && esperado.equalsIgnoreCase(actual);
    }

    private boolean unidadCompatible(
            CriterioConsumoSuplementos criterio, PcsObservedValue observado) {
        return unidadCompatible(criterio, observado.unidad());
    }

    private boolean unidadCompatible(CriterioConsumoSuplementos criterio, String unidad) {
        String esperada =
                criterio.getUnidadNormalizada() != null
                        ? criterio.getUnidadNormalizada()
                        : criterio.getUnidadReferencia();
        return esperada == null
                || esperada.isBlank()
                || unidad != null && esperada.equalsIgnoreCase(unidad);
    }

    private boolean comparar(
            BigDecimal observado,
            OperadorCriterioPcs operador,
            BigDecimal referencia,
            BigDecimal referenciaHasta) {
        int comparacion = observado.compareTo(referencia);
        return switch (operador) {
            case MAYOR_QUE -> comparacion > 0;
            case MAYOR_O_IGUAL -> comparacion >= 0;
            case MENOR_QUE -> comparacion < 0;
            case MENOR_O_IGUAL -> comparacion <= 0;
            case IGUAL -> comparacion == 0;
            case ENTRE -> comparacion >= 0 && observado.compareTo(referenciaHasta) <= 0;
        };
    }

    private PcsCriterionTrace noCalculable(
            CriterioConsumoSuplementos criterio, PcsObservedValue observado, String motivo) {
        return new PcsCriterionTrace(
                criterio.getId(),
                criterio.getComponenteTipo(),
                null,
                null,
                observado == null ? null : observado.datoObservado(),
                criterio.getAmbitoAporte(),
                observado == null ? null : observado.unidad(),
                criterio.getOperador(),
                criterio.getCantidadReferencia(),
                criterio.getCantidadReferenciaHasta(),
                ResultadoCriterioPcs.NO_CALCULABLE,
                motivo);
    }

    private PcsCriterionTrace noCalculable(
            CriterioConsumoSuplementos criterio, PcsComponentContribution aporte, String motivo) {
        return traza(criterio, aporte, null, ResultadoCriterioPcs.NO_CALCULABLE, motivo);
    }

    private PcsCriterionTrace traza(
            CriterioConsumoSuplementos criterio,
            PcsComponentContribution aporte,
            BigDecimal observado,
            ResultadoCriterioPcs resultado,
            String motivo) {
        return new PcsCriterionTrace(
                criterio.getId(),
                criterio.getComponenteTipo(),
                aporte == null ? null : aporte.aporteAlimentos(),
                aporte == null ? null : aporte.aporteSuplementos(),
                observado,
                criterio.getAmbitoAporte(),
                aporte == null ? null : aporte.unidad(),
                criterio.getOperador(),
                criterio.getCantidadReferencia(),
                criterio.getCantidadReferenciaHasta(),
                resultado,
                motivo);
    }

    private boolean aplicable(RubricaConsumoSuplementos rubrica, PcsFactualSnapshot snapshot) {
        if (rubrica == null
                || snapshot == null
                || snapshot.fechaCorte() == null
                || rubrica.getEstado() != EstadoCriterioConsumo.ACTIVO
                || !rubrica.isValidada()) {
            return false;
        }
        var inicio = snapshot.fechaCorte().atStartOfDay(ZoneOffset.UTC).toInstant();
        var fin =
                snapshot.fechaCorte()
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .minusNanos(1);
        return (rubrica.getVigenteDesde() == null || !rubrica.getVigenteDesde().isAfter(fin))
                && (rubrica.getVigenteHasta() == null
                        || !rubrica.getVigenteHasta().isBefore(inicio));
    }

    private PcsEvaluationResult resultadoNoDeterminado(
            PcsFactualSnapshot snapshot,
            RubricaConsumoSuplementos rubrica,
            List<PcsCriterionTrace> trazas,
            String motivo) {
        return resultado(
                snapshot, rubrica, trazas, null, EstadoClasificacionConsumo.NO_DETERMINADA, motivo);
    }

    private PcsEvaluationResult resultado(
            PcsFactualSnapshot snapshot,
            RubricaConsumoSuplementos rubrica,
            List<PcsCriterionTrace> trazas,
            Boolean altoConsumo,
            EstadoClasificacionConsumo estado,
            String motivo) {
        int noCalculables =
                (int)
                        trazas.stream()
                                .filter(
                                        item ->
                                                item.resultado()
                                                        == ResultadoCriterioPcs.NO_CALCULABLE)
                                .count();
        int cumplidos =
                (int)
                        trazas.stream()
                                .filter(item -> item.resultado() == ResultadoCriterioPcs.CUMPLE)
                                .count();
        return new PcsEvaluationResult(
                altoConsumo,
                estado,
                motivo,
                trazas.size() - noCalculables,
                cumplidos,
                noCalculables,
                rubrica == null ? null : rubrica.getId(),
                snapshot == null ? null : snapshot.fechaCorte(),
                trazas);
    }
}
