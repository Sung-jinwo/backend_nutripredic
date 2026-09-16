package com.backend.nutri_predic.prediccionmodelo.ciclo.dto;

import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoResponse;
import java.time.LocalDate;
import java.util.List;

public record CicloDiarioResponse(
        String estado, LocalDate fechaCorte, LocalDate fechaEvaluada,
        boolean reutilizado, Long prediccionId, String mensaje,
        List<String> datosFaltantes, AnalisisPredictivoResponse analisis) {
    public static CicloDiarioResponse pendiente(LocalDate fecha, List<String> faltantes) {
        return new CicloDiarioResponse("PENDIENTE", fecha, fecha.minusDays(1), false, null,
                "Completa el registro de alimentos, macronutrientes y agua del día anterior.", faltantes, null);
    }

    public static CicloDiarioResponse fallido(
            LocalDate fecha, Long prediccionId, boolean reutilizado,
            String mensaje, AnalisisPredictivoResponse analisis) {
        return new CicloDiarioResponse("FALLIDO", fecha, fecha.minusDays(1), reutilizado,
                prediccionId, mensaje, List.of(), analisis);
    }
}
