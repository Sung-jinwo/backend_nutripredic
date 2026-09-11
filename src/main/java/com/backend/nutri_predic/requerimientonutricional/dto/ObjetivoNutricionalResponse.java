package com.backend.nutri_predic.requerimientonutricional.dto;

import com.backend.nutri_predic.actividadfisica.dto.NivelActividadFisicaResponse;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import java.math.BigDecimal;
import java.util.List;

public record ObjetivoNutricionalResponse(
        String estado,
        TipoObjetivoFisico objetivoFisico,
        ObjetivoEnergetico objetivoEnergetico,
        NivelActividadFisicaResponse actividad,
        BigDecimal kcalObjetivo,
        BigDecimal proteinaObjetivoG,
        BigDecimal carbohidratosObjetivoG,
        BigDecimal grasasObjetivoG,
        String fuenteReferencia,
        String versionReferencia,
        String motivo,
        List<String> pendientes) {

    public static ObjetivoNutricionalResponse noDisponible(String motivo, List<String> pendientes,
            TipoObjetivoFisico objetivoFisico, ObjetivoEnergetico objetivoEnergetico,
            NivelActividadFisicaResponse actividad) {
        return new ObjetivoNutricionalResponse("NO_DISPONIBLE", objetivoFisico, objetivoEnergetico,
                actividad, null, null, null, null, null, null, motivo, pendientes);
    }
}
