package com.backend.nutri_predic.requerimientonutricional.dto;
import java.math.BigDecimal;
public record RequerimientoNutricionalResponse(
        String estado, String fuenteReferencia, String versionReferencia, BigDecimal kcalObjetivo, BigDecimal proteinaObjetivoG,
        BigDecimal carbohidratosObjetivoG, BigDecimal grasasObjetivoG, String tipoResultado, String motivoNoDisponible) {
    public static RequerimientoNutricionalResponse noDisponible(String motivo) {
        return new RequerimientoNutricionalResponse("NO_DISPONIBLE", null, null, null, null, null, null, null, motivo);
    }
}
