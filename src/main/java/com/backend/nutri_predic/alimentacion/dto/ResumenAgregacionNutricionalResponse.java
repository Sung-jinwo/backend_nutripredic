package com.backend.nutri_predic.alimentacion.dto;

import java.util.List;

public record ResumenAgregacionNutricionalResponse(
        List<ResumenNutricionalDiario> dias, ResumenNutricionalVentana ventana) {}
