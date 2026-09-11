package com.backend.nutri_predic.observaciondiaria.dto;

import java.time.LocalDate;

public record CompletitudVentanaV5Response(
        boolean ventanaCompleta,
        int diasCompletos,
        int diasEsperados,
        LocalDate fechaDesde,
        LocalDate fechaCorte
) {
    public CompletitudVentanaV5Response(boolean ventanaCompleta, int diasCompletos, int diasEsperados) {
        this(ventanaCompleta, diasCompletos, diasEsperados, null, null);
    }
}
