package com.backend.nutri_predic.suplemento.dto;

import java.math.*;

public record AporteSuplementoCalculado(
        boolean calculable,
        BigDecimal proteinaG,
        BigDecimal creatinaG,
        BigDecimal cafeinaMg,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal sodioMg) {
    public static AporteSuplementoCalculado noCalculable() {
        return new AporteSuplementoCalculado(false, null, null, null, null, null, null);
    }
}
