package com.backend.nutri_predic.datasetmodelov5.dto;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EstadoRubricaPerfilHabitos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PendientesEntrenamientoV5Response(
        LocalDate fechaCorte,
        int xTotal,
        String criterioPrioridad,
        Resumen resumen,
        List<Candidato> candidatos) {

    public record Resumen(
            long totalClientesRevisados,
            long totalCandidatosNoSmokeConPendientes,
            long totalExcluidosSmokeTecnico,
            long totalAptosOmitidos,
            long filasTrainables,
            Map<String, Long> distribucionClases,
            boolean datasetV5ListoParaEntrenar) {}

    public record Candidato(
            int prioridadOrden,
            Long clienteId,
            LocalDate fechaCorte,
            DatosObservacionales datosObservacionales,
            GroundTruth groundTruth,
            boolean smokeTecnico,
            boolean aptoParaEntrenamiento) {}

    public record DatosObservacionales(
            boolean perfilCompleto,
            boolean perfilFaltante,
            boolean objetivoFisicoFaltante,
            Ventana alimentacion,
            Ventana suplementacion,
            Ventana habitos,
            int xDisponibles,
            int xTotal,
            List<String> featuresXFaltantes,
            Map<String, String> motivosXNull,
            List<String> pendientes) {}

    public record Ventana(boolean completa, int diasCompletos, int diasFaltantes) {}

    public record GroundTruth(
            boolean disponible,
            Long evaluacionId,
            EstadoValidezMedicion estadoValidez,
            Boolean rubricaValida,
            EstadoRubricaPerfilHabitos estadoRubrica,
            String rubricaCodigo,
            BigDecimal rubricaVersion,
            ClasificacionPerfilHabitos clasificacionReal,
            boolean groundTruthValido) {}
}
