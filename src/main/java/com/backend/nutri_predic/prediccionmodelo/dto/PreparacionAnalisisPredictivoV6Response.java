package com.backend.nutri_predic.prediccionmodelo.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PreparacionAnalisisPredictivoV6Response(
        Long clienteId,
        LocalDate fechaCorte,
        boolean puedeAnalizar,
        int diasCompletos,
        int diasRequeridos,
        Map<String, DominioPreparacionAnalisisResponse> dominios,
        boolean perfilHistoricoDisponible,
        int xDisponibles,
        int xTotal,
        List<String> datosFaltantes) {}
