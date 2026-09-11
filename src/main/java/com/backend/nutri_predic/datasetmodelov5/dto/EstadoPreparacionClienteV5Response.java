package com.backend.nutri_predic.datasetmodelov5.dto;

import java.time.LocalDate;
import java.util.*;

public record EstadoPreparacionClienteV5Response(
        Long clienteId,
        LocalDate fechaCorte,
        boolean perfilCompleto,
        int alimentacionDiasCompletos,
        int suplementacionDiasCompletos,
        int habitosDiasCompletos,
        int nutrientesCalculables,
        int suplementosCalculables,
        int camposHabitosCompletos,
        boolean groundTruthValido,
        boolean smokeTecnico,
        int xDisponibles,
        boolean aptoParaEntrenamiento,
        List<String> pendientes,
        Map<String, String> motivosXNull) {}
