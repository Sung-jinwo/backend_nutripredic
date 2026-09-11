package com.backend.nutri_predic.actividadfisica.dto;

import com.backend.nutri_predic.common.enums.NivelActividadFisica;
import java.math.BigDecimal;
import java.time.LocalDate;

public record NivelActividadFisicaResponse(
        NivelActividadFisica nivel,
        String fuenteClasificacion,
        LocalDate fechaEvaluacion,
        BigDecimal metMinSemana,
        String reglaVersion,
        String fuenteReferencia,
        String versionReferencia,
        String estadoRegla,
        Boolean utilizableParaRequerimiento,
        String motivoNoDeterminada) {
    public static NivelActividadFisicaResponse noDeterminada(String fuente, String motivo, String reglaVersion) {
        return new NivelActividadFisicaResponse(
                NivelActividadFisica.NO_DETERMINADA, fuente, null, null, reglaVersion, null, null, "NO_DETERMINADA", false, motivo);
    }
}
