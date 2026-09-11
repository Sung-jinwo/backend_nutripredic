package com.backend.nutri_predic.suplemento.dto;

import java.math.*;
import java.time.*;

public record ResumenSuplementacionVentana(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        int diasVentana,
        int diasConConsumoSuplemento,
        int diasConConsumoCalculable,
        int registrosConsumoTotal,
        int registrosCalculables,
        int registrosNoCalculables,
        BigDecimal porcentajeRegistrosCalculables,
        BigDecimal proteinaSuplementariaGTotal,
        BigDecimal creatinaGTotal,
        BigDecimal cafeinaMgTotal,
        BigDecimal carbohidratosSuplementariosGTotal,
        BigDecimal grasasSuplementariasGTotal,
        BigDecimal sodioMgTotal,
        BigDecimal promedioProteinaSobreVentana,
        BigDecimal promedioProteinaSobreDiasConConsumo) {}
