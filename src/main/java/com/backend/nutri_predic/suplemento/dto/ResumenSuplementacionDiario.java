package com.backend.nutri_predic.suplemento.dto;

import java.math.*;
import java.time.*;

public record ResumenSuplementacionDiario(
        LocalDate fecha,
        BigDecimal proteinaSuplementariaG,
        BigDecimal creatinaG,
        BigDecimal cafeinaMg,
        BigDecimal carbohidratosSuplementariosG,
        BigDecimal grasasSuplementariasG,
        BigDecimal sodioMg,
        int registrosConsumoTotal,
        int registrosCalculables,
        int registrosNoCalculables,
        BigDecimal porcentajeCobertura) {}
