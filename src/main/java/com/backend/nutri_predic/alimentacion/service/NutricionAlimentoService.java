package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.AporteNutricionalCalculado;
import com.backend.nutri_predic.alimentacion.entity.*;
import java.math.*;
import org.springframework.stereotype.Service;

@Service
public class NutricionAlimentoService {
    private final ConversionUnidadAlimentoService conversion;

    public NutricionAlimentoService(ConversionUnidadAlimentoService c) {
        conversion = c;
    }

    public AporteNutricionalCalculado calcular(RegistroAlimento r) {
        if (r != null && r.getKcalRegistrada() != null && r.getProteinaGRegistrada() != null && r.getCarbohidratosGRegistrados() != null && r.getGrasasGRegistradas() != null)
            return new AporteNutricionalCalculado(true, r.getKcalRegistrada(), r.getProteinaGRegistrada(), r.getCarbohidratosGRegistrados(), r.getGrasasGRegistradas(), null, null, null);
        return calcular(r, r == null ? null : r.getComposicionNutricional());
    }

    public AporteNutricionalCalculado calcular(
            RegistroAlimento r, ComposicionNutricionalAlimento c) {
        if (r == null
                || c == null
                || r.getAlimento() == null
                || c.getAlimento() == null
                || !r.getAlimento().getId().equals(c.getAlimento().getId())
                || c.getCantidadReferencia() == null
                || c.getCantidadReferencia().signum() <= 0
                || c.getUnidadReferencia() == null)
            return AporteNutricionalCalculado.noCalculable();
        var x = conversion.convertir(r, c.getUnidadReferencia().getCodigo());
        if (!x.calculable()) return AporteNutricionalCalculado.noCalculable();
        var f = x.cantidadConvertida().divide(c.getCantidadReferencia(), 12, RoundingMode.HALF_UP);
        return new AporteNutricionalCalculado(
                true,
                e(c.getKcal(), f),
                e(c.getProteinaG(), f),
                e(c.getCarbohidratosG(), f),
                e(c.getGrasasG(), f),
                e(c.getFibraG(), f),
                e(c.getAzucarG(), f),
                e(c.getSodioMg(), f));
    }

    private BigDecimal e(BigDecimal v, BigDecimal f) {
        return v == null ? null : v.multiply(f);
    }
}
