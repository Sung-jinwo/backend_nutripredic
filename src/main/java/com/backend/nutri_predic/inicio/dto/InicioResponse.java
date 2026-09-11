package com.backend.nutri_predic.inicio.dto;

public record InicioResponse(
        Object objetivoNutricional,
        Object resumenDiario,
        Object preparacionV6,
        Object ultimaPrediccion,
        Object siguientePlan,
        Object aprendizajePendiente,
        String siguienteAccion) {}
