package com.backend.nutri_predic.suplemento.dto;

import java.time.*;
import java.util.*;

public record ResumenSuplementacionResponse(
        LocalDate fechaCorte,
        List<ResumenSuplementacionDiario> resumenDiario,
        ResumenSuplementacionVentana resumenVentana,
        List<SuplementoClienteHistoricoResult> suplementacionHabitual,
        int cantidadSuplementosHabitualesAplicables,
        int cantidadSuplementosHabitualesActivos) {}
