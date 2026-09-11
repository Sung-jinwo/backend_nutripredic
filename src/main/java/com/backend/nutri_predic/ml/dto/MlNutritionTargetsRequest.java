package com.backend.nutri_predic.ml.dto;

import com.backend.nutri_predic.cliente.entity.Cliente;
import java.math.BigDecimal;

public record MlNutritionTargetsRequest(
        Integer edad,
        BigDecimal pesoKg,
        BigDecimal alturaCm,
        String sexoBiologico,
        String objetivoEnergetico,
        Integer diasEntrenamientoSemana,
        Integer duracionPromedioSesionMinutos) {

    public static MlNutritionTargetsRequest from(Cliente cliente) {
        return new MlNutritionTargetsRequest(
                cliente.getEdad(),
                cliente.getPesoKg(),
                cliente.getAlturaCm(),
                cliente.getSexo() == null ? null : cliente.getSexo().name(),
                cliente.getObjetivoEnergetico() == null
                        ? null
                        : cliente.getObjetivoEnergetico().name(),
                cliente.getDiasEntrenamientoSemana(),
                cliente.getDuracionPromedioSesionMinutos());
    }
}
