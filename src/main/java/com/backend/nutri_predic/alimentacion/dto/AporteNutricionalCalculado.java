package com.backend.nutri_predic.alimentacion.dto;

import java.math.BigDecimal;

public record AporteNutricionalCalculado(
        boolean calculable,
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal fibraG,
        BigDecimal azucarG,
        BigDecimal sodioMg) {
    public static AporteNutricionalCalculado noCalculable() {
        return new AporteNutricionalCalculado(false, null, null, null, null, null, null, null);
    }
}
