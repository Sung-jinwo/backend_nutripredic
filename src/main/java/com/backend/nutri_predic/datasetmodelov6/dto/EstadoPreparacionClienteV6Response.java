package com.backend.nutri_predic.datasetmodelov6.dto;
import java.time.LocalDate; import java.util.List; import java.util.Map;
public record EstadoPreparacionClienteV6Response(Long clienteId, LocalDate fechaCorte,
        int xDisponibles, int xTotal, boolean perfilHistorico, boolean groundTruthValido,
        boolean smokeTecnico, boolean aptoParaEntrenamiento, List<String> pendientes,
        Map<String, String> motivosXNull) {}
